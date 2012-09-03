package com.higheranimals.measurewords;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MeasureWordsActivity extends Activity {
    private static final String TAG = "MeasureWordsActivity";
    private int expectedAnswer = -1;
    private int correctCount = 0;
    private int incorrectCount = 0;
    Cursor cur;

    private final int QUESTION_COUNT = 5;

    // Buttons
    private Button[] buttons;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "constructor");
        if (savedInstanceState != null) {
            correctCount = savedInstanceState.getInt("correctCount");
            incorrectCount = savedInstanceState.getInt("incorrectCount");
        }
        if (DbHelper.databaseExists(this)) {
            init();
        } else {
            setContentView(R.layout.loading);
            (new AsyncTask<Context, Integer, Boolean>() {

                @Override
                protected Boolean doInBackground(Context... contexts) {
                    Log.v(TAG, "doInBackground");
                    try {
                        QuestionProvider.initializeDataIfNecessary(contexts[0]);
                    } catch (IOException e) {
                        Log.v(TAG, e.toString());
                        return false;
                    }
                    this.publishProgress(100);
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

    private void init() {
        this.setContentView(R.layout.main);
        Uri uri = QuestionProvider.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(QuestionProvider.UriParameter.LIMIT,
                        Integer.toString(QUESTION_COUNT)).build();
        // TODO pare this down a bit -- probably don't need all
        String[] projection = new String[] { QuestionProvider.Field.NOUN_ID,
                QuestionProvider.Field.NOUN_HANZI,
                QuestionProvider.Field.NOUN_PINYIN,
                QuestionProvider.Field.NOUN_ENGLISH,
                QuestionProvider.Field.MEASURE_WORD_ID,
                QuestionProvider.Field.MEASURE_WORD_HANZI,
                QuestionProvider.Field.MEASURE_WORD_PINYIN,
                QuestionProvider.Field.MEASURE_WORD_ENGLISH,
                QuestionProvider.Field.CORRECT,
                QuestionProvider.Field.INCORRECT };
        cur = getContentResolver().query(uri, projection, null, null,
                "RANDOM()");
        this.startManagingCursor(cur);
        assignButtons();
        if (expectedAnswer == -1) {
            composeQuestion();
        }
        setCorrectDisplay(correctCount);
        setIncorrectDisplay(incorrectCount);
    }

    private void assignButtons() {
        Button button0 = (Button) findViewById(R.id.button0);
        button0.setOnClickListener(new ValueClickListener(0));
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new ValueClickListener(1));
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new ValueClickListener(2));
        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new ValueClickListener(3));
        buttons = new Button[] { button0, button1, button2, button3 };
    }

    private void composeQuestion() {
        Log.v(TAG, "composeQuestion");
        if (cur.moveToNext()) {
            int noun_id = cur.getInt(cur
                    .getColumnIndex(QuestionProvider.Field.NOUN_ID));
            String noun_hanzi = cur.getString(cur
                    .getColumnIndex(QuestionProvider.Field.NOUN_HANZI));
            ((TextView) this.findViewById(R.id.hanzi)).setText(noun_hanzi);
            String noun_pinyin = cur.getString(cur
                    .getColumnIndex(QuestionProvider.Field.NOUN_PINYIN));
            ((TextView) this.findViewById(R.id.pinyin)).setText(noun_pinyin);
            String noun_english = cur.getString(cur
                    .getColumnIndex(QuestionProvider.Field.NOUN_ENGLISH));
            ((TextView) this.findViewById(R.id.english)).setText(noun_english);
            int measure_word_id = cur.getInt(cur
                    .getColumnIndex(QuestionProvider.Field.MEASURE_WORD_ID));
            String measure_word_hanzi = cur.getString(cur
                    .getColumnIndex(QuestionProvider.Field.MEASURE_WORD_HANZI));
            Log.v(TAG, "noun pinyin: " + noun_pinyin + "; noun_english: "
                    + noun_english);
            // Get wrong answers
            Uri uri = QuestionProvider.CONTENT_URI
                    .buildUpon()
                    .appendQueryParameter(
                            QuestionProvider.UriParameter.DISTINCT, "1")
                    .appendQueryParameter(QuestionProvider.UriParameter.LIMIT,
                            Integer.toString(3)).build();
            Cursor optionCursor = getContentResolver().query(
                    uri,
                    new String[] { QuestionProvider.Field.MEASURE_WORD_HANZI },
                    QuestionProvider.Field.NOUN_ID + " != ? AND "
                            + QuestionProvider.Field.MEASURE_WORD_ID + " != ?",
                    new String[] { Integer.toString(noun_id),
                            Integer.toString(measure_word_id) }, "RANDOM()");
            // Set wrong answers
            // TODO make the number of options dynamic
            List<Integer> indices = Arrays.asList(0, 1, 2, 3);
            Collections.shuffle(indices);
            int i = 0;
            while (optionCursor.moveToNext()) {
                String wrong_measure_word_hanzi = optionCursor
                        .getString(optionCursor
                                .getColumnIndex(QuestionProvider.Field.MEASURE_WORD_HANZI));
                buttons[indices.get(i++)].setText(wrong_measure_word_hanzi);
            }
            optionCursor.close();
            expectedAnswer = indices.get(i);
            buttons[expectedAnswer].setText(measure_word_hanzi);
        } else {
            // TODO compose all questions at once and bundle so rotation does
            // not reset question list.
            this.finish();
        }
    }

    private boolean checkAnswer(int value) {
        return this.expectedAnswer == value;
    }

    private void handleAnswer(int value) {
        if (checkAnswer(value)) {
            setCorrectDisplay(++this.correctCount);
        } else {
            setIncorrectDisplay(++this.incorrectCount);
        }
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
        savedInstanceState.putInt("correctCount", this.correctCount);
        savedInstanceState.putInt("incorrectCount", this.incorrectCount);
    }
}