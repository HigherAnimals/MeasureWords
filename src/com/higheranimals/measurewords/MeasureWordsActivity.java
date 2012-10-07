package com.higheranimals.measurewords;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MeasureWordsActivity extends Activity {
    private static final String TAG = "MeasureWordsActivity";
    private int questionIndex = 0;
    private int correctCount = 0;
    private int incorrectCount = 0;
    private ArrayList<Question> questionList;

    private final int QUESTION_COUNT = 5;
    private final int ANSWERS_COUNT = 4;

    // Buttons
    private LinearLayout[] rows;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "constructor");
        rows = new LinearLayout[2];
        if (savedInstanceState != null) { // ready
            correctCount = savedInstanceState.getInt("correctCount");
            incorrectCount = savedInstanceState.getInt("incorrectCount");
            questionIndex = savedInstanceState.getInt("questionIndex");
            questionList = savedInstanceState
                    .getParcelableArrayList("questionList");
            init();
        } else { // loading
            setContentView(R.layout.loading);
            questionList = new ArrayList<Question>();
            if (DbHelper.databaseExists(this)) {
                generateQuestions();
            } else {
                (new AsyncTask<Context, Integer, Boolean>() {

                    @Override
                    protected Boolean doInBackground(Context... contexts) {
                        Log.v(TAG, "doInBackground");
                        try {
                            QuestionProvider
                                    .initializeDataIfNecessary(contexts[0]);
                        } catch (IOException e) {
                            // Trouble finding/initializing database.
                            Log.v(TAG, e.toString());
                            return false;
                        }
                        return true;
                    }

                    @Override
                    protected void onProgressUpdate(Integer... integers) {
                        Log.v(TAG, "onProgressUpdate: " + integers[0]);
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        Log.v(TAG, "onPostExecute");
                        if (success) {
                            MeasureWordsActivity.this.init();
                        } else {
                            // TODO add error announcement
                            MeasureWordsActivity.this.finish();
                        }
                    }
                }).execute(this);
            }
        }
    }

    private void generateQuestions() {
        (new AsyncTask<Integer, Integer, ArrayList<Question>>() {

            @Override
            protected ArrayList<Question> doInBackground(Integer... params) {
                Uri uri = QuestionProvider.CONTENT_URI
                        .buildUpon()
                        .appendQueryParameter(
                                QuestionProvider.UriParameter.LIMIT,
                                Integer.toString(QUESTION_COUNT))
                        .appendQueryParameter(
                                QuestionProvider.UriParameter.DISTINCT, "1")
                        .build();
                // TODO pare this down a bit -- probably don't need all
                String[] projection = new String[] {
                        QuestionProvider.Field.NOUN_ID,
                        QuestionProvider.Field.NOUN_HANZI,
                        QuestionProvider.Field.NOUN_PINYIN,
                        QuestionProvider.Field.NOUN_ENGLISH,
                        QuestionProvider.Field.MEASURE_WORD_ID,
                        QuestionProvider.Field.MEASURE_WORD_HANZI,
                        QuestionProvider.Field.MEASURE_WORD_PINYIN,
                        QuestionProvider.Field.MEASURE_WORD_ENGLISH,
                        QuestionProvider.Field.QUESTION_ID };
                ContentResolver contentResolver = getContentResolver();
                Cursor cur = contentResolver.query(uri, projection, null, null,
                        QuestionProvider.Ordering.HARDEST);
                while (cur.moveToNext()) {
                    Uri measureWordsUri = QuestionProvider.CONTENT_URI
                            .buildUpon()
                            .appendQueryParameter(
                                    QuestionProvider.UriParameter.LIMIT,
                                    Integer.toString(ANSWERS_COUNT - 1))
                            .appendQueryParameter(
                                    QuestionProvider.UriParameter.DISTINCT, "1")
                            .build();
                    int questionId = cur
                            .getInt(cur
                                    .getColumnIndex(QuestionProvider.Field.QUESTION_ID));
                    int measureWordId = cur
                            .getInt(cur
                                    .getColumnIndex(QuestionProvider.Field.MEASURE_WORD_ID));
                    int nounId = cur.getInt(cur
                            .getColumnIndex(QuestionProvider.Field.NOUN_ID));
                    String nounHanzi = cur.getString(cur
                            .getColumnIndex(QuestionProvider.Field.NOUN_HANZI));
                    String nounPinyin = cur
                            .getString(cur
                                    .getColumnIndex(QuestionProvider.Field.NOUN_PINYIN));
                    String nounEnglish = cur
                            .getString(cur
                                    .getColumnIndex(QuestionProvider.Field.NOUN_ENGLISH));
                    String measureWordHanzi = cur
                            .getString(cur
                                    .getColumnIndex(QuestionProvider.Field.MEASURE_WORD_HANZI));
                    String measureWordPinyin = cur
                            .getString(cur
                                    .getColumnIndex(QuestionProvider.Field.MEASURE_WORD_PINYIN));
                    String measureWordEnglish = cur
                            .getString(cur
                                    .getColumnIndex(QuestionProvider.Field.MEASURE_WORD_ENGLISH));
                    Cursor measureWordsCur = contentResolver
                            .query(measureWordsUri,
                                    new String[] {
                                            QuestionProvider.Field.MEASURE_WORD_HANZI,
                                            QuestionProvider.Field.MEASURE_WORD_PINYIN,
                                            QuestionProvider.Field.MEASURE_WORD_ENGLISH },
                                    QuestionProvider.Field.NOUN_ID
                                            + " != ? AND "
                                            + QuestionProvider.Field.MEASURE_WORD_ID
                                            + " != ?",
                                    new String[] { Integer.toString(nounId),
                                            Integer.toString(measureWordId) },
                                    QuestionProvider.Ordering.RANDOM);
                    ArrayList<Answer> AnswerList = new ArrayList<Answer>();
                    while (measureWordsCur.moveToNext()) {
                        AnswerList
                                .add(new Answer(
                                        measureWordsCur
                                                .getString(measureWordsCur
                                                        .getColumnIndex(QuestionProvider.Field.MEASURE_WORD_HANZI)),
                                        measureWordsCur.getString(measureWordsCur
                                                .getColumnIndex(QuestionProvider.Field.MEASURE_WORD_PINYIN)),
                                        measureWordsCur.getString(measureWordsCur
                                                .getColumnIndex(QuestionProvider.Field.MEASURE_WORD_ENGLISH))));
                    }
                    measureWordsCur.close();
                    questionList.add(new Question(questionId, nounHanzi,
                            nounPinyin, nounEnglish, new Answer(
                                    measureWordHanzi, measureWordPinyin,
                                    measureWordEnglish), AnswerList));
                }
                cur.close();
                return questionList;
            }

            @Override
            protected void onPostExecute(ArrayList<Question> questionList) {
                if (questionList != null) {
                    MeasureWordsActivity.this.questionList = questionList;
                    init();
                } else {
                    // TODO add error announcement
                    MeasureWordsActivity.this.finish();
                }
            }
        }).execute(0);
    }

    private void init() {
        Log.v(TAG, "init");
        setContentView(R.layout.main);
        // Find rows.
        rows[0] = (LinearLayout) findViewById(R.id.row0);
        rows[1] = (LinearLayout) findViewById(R.id.row1);
        setCorrectDisplay(correctCount);
        setIncorrectDisplay(incorrectCount);
        composeQuestion();
    }

    private void composeQuestion() {
        Log.v(TAG, "composeQuestion");
        Question question = getCurrentQuestion();
        // Set noun
        ((TextView) findViewById(R.id.hanzi)).setText(question.getNounHanzi());
        ((TextView) findViewById(R.id.pinyin))
                .setText(question.getNounPinyin());
        ((TextView) findViewById(R.id.english)).setText(question
                .getNounEnglish());
        List<Answer> answerList = question.getAnswers();
        LayoutInflater layoutInflater = getLayoutInflater();
        // Clear rows.
        for (int i = 0; i < rows.length; ++i) {
            rows[i].removeAllViews();
        }
        // Populate rows.
        for (int i = 0; i < answerList.size(); ++i) {
            Answer answer = answerList.get(i);
            View choice = layoutInflater.inflate(R.layout.answer, null);
            choice.setOnClickListener(new ValueClickListener(i));
            TextView hanziView = (TextView) choice.findViewById(R.id.mwHanzi);
            hanziView.setText(answer.getHanzi());
            TextView pinyinView = (TextView) choice.findViewById(R.id.mwPinyin);
            pinyinView.setText(answer.getPinyin());
            rows[i % 2].addView(choice);
        }
    }

    private Question getCurrentQuestion() {
        return questionList.get(questionIndex);
    }

    private void handleAnswer(int i) {
        Log.v(TAG, "handleAnswer");
        if (getCurrentQuestion().isCorrectChoice(i)) {
            setCorrectDisplay(++this.correctCount);
        } else {
            setIncorrectDisplay(++this.incorrectCount);
        }
        ++questionIndex;
        composeQuestion();
    }

    private void setCorrectDisplay(int count) {
        // TODO Consider a TextSwitcher
        ((TextView) findViewById(R.id.correctCount)).setText(Integer
                .toString(count));
    }

    private void setIncorrectDisplay(int count) {
        // TODO Consider a TextSwitcher
        ((TextView) findViewById(R.id.incorrectCount)).setText(Integer
                .toString(count));
    }

    private class ValueClickListener implements View.OnClickListener {
        private final int value;

        public ValueClickListener(int value) {
            this.value = value;
        }

        @Override
        public void onClick(View v) {
            MeasureWordsActivity.this.handleAnswer(this.value);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("correctCount", correctCount);
        savedInstanceState.putInt("incorrectCount", incorrectCount);
        savedInstanceState.putInt("questionIndex", questionIndex);
        savedInstanceState.putParcelableArrayList("questionList", questionList);
    }
}