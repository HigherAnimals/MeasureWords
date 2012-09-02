package com.higheranimals.measurewords;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    private SQLiteDatabase db = null;
    Cursor cur;

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
            init(new DbHelper(this).getWritableDatabase());
        } else {
            setContentView(R.layout.loading);
            (new AsyncTask<Context, Integer, SQLiteDatabase>() {

                @Override
                protected SQLiteDatabase doInBackground(Context... contexts) {
                    Log.v(TAG, "doInBackground");
                    try {
                        DbHelper.createDatabaseIfNotExists(contexts[0]);
                    } catch (IOException e) {
                        Log.v(TAG, e.toString());
                        return null;
                    }
                    this.publishProgress(100);
                    return new DbHelper(MeasureWordsActivity.this)
                            .getWritableDatabase();
                }

                @Override
                protected void onProgressUpdate(Integer... integers) {
                    Log.v(TAG, "onProgressUpdate: " + integers[0]);
                }

                @Override
                protected void onPostExecute(SQLiteDatabase db) {
                    Log.v(TAG, "onPostExecute");
                    if (db != null) {
                        MeasureWordsActivity.this.init(db);
                    } else {
                        // TODO add error announcement
                        MeasureWordsActivity.this.finish();
                    }
                }
            }).execute(this);
        }
    }

    private void init(SQLiteDatabase db) {
        this.setContentView(R.layout.main);
        this.db = db;
        // TODO clean this up;
        cur = db.rawQuery(
                "SELECT nouns._id AS noun_id, nouns.hanzi AS noun_hanzi, nouns.pinyin AS noun_pinyin, nouns.english AS noun_english, measure_words.hanzi AS measure_word_hanzi, measure_words.pinyin AS measure_word_pinyin, measure_words.english AS measure_word_english FROM nouns LEFT OUTER JOIN nouns_measure_words ON nouns._id = nouns_measure_words.noun_id INNER JOIN measure_words on nouns_measure_words.measure_word_id = measure_words._id ORDER BY RANDOM();",
                null);
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
            int noun_id = cur.getInt(cur.getColumnIndex("noun_id"));
            String noun_hanzi = cur.getString(cur.getColumnIndex("noun_hanzi"));
            ((TextView) this.findViewById(R.id.hanzi)).setText(noun_hanzi);
            String noun_pinyin = cur.getString(cur
                    .getColumnIndex("noun_pinyin"));
            ((TextView) this.findViewById(R.id.pinyin)).setText(noun_pinyin);
            String noun_english = cur.getString(cur
                    .getColumnIndex("noun_english"));
            ((TextView) this.findViewById(R.id.english)).setText(noun_english);
            String measure_word_hanzi = cur.getString(cur
                    .getColumnIndex("measure_word_hanzi"));
            Log.v(TAG, "noun pinyin: " + noun_pinyin + "; noun_english: "
                    + noun_english);
            // Get wrong answers
            Cursor optionCursor = db
                    .rawQuery(
                            "SELECT DISTINCT measure_words.hanzi AS measure_word_hanzi FROM measure_words WHERE measure_words._id NOT IN (SELECT measure_words._id FROM measure_words INNER JOIN nouns_measure_words ON measure_words._id = nouns_measure_words.measure_word_id INNER JOIN nouns ON nouns._id = nouns_measure_words.noun_id WHERE nouns._id = "
                                    + noun_id + ") ORDER BY RANDOM() LIMIT 3;",
                            null);
            // Set wrong answers
            List<Integer> indices = Arrays.asList(0, 1, 2, 3);
            Collections.shuffle(indices);
            int i = 0;
            while (optionCursor.moveToNext()) {
                String wrong_measure_word_hanzi = optionCursor
                        .getString(optionCursor
                                .getColumnIndex("measure_word_hanzi"));
                buttons[indices.get(i++)].setText(wrong_measure_word_hanzi);
            }
            optionCursor.close();
            expectedAnswer = indices.get(i);
            buttons[expectedAnswer].setText(measure_word_hanzi);
            // TODO actually set up a question.
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }
}