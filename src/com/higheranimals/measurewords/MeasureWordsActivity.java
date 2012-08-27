package com.higheranimals.measurewords;

import java.io.IOException;

import android.app.Activity;
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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "constructor");
        setContentView(R.layout.main);
        try {
            DbHelper.createDatabaseIfNotExists(this);
        } catch (IOException e) {
            Log.v(TAG, e.toString());
        }
        setListeners();
        composeQuestion();
    }

    private void setListeners() {
        ((Button) findViewById(R.id.button0))
                .setOnClickListener(new ValueClickListener(0));
        ((Button) findViewById(R.id.button1))
                .setOnClickListener(new ValueClickListener(1));
        ((Button) findViewById(R.id.button2))
                .setOnClickListener(new ValueClickListener(2));
        ((Button) findViewById(R.id.button3))
                .setOnClickListener(new ValueClickListener(3));
    }

    private void composeQuestion() {
        // TODO actually set up a question.
        this.expectedAnswer = 1;
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
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(TAG, "onRestoreInstanceState");
        correctCount = savedInstanceState.getInt("correctCount");
        incorrectCount = savedInstanceState.getInt("incorrectCount");
        setCorrectDisplay(correctCount);
        setIncorrectDisplay(incorrectCount);
    }
}