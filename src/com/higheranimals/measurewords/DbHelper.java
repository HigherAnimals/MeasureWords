package com.higheranimals.measurewords;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

    static final private String TAG = "DbHelper";
    static private String DB_PATH = "/data/data/com.higheranimals.measurewords/databases/";
    static private String DB_NAME = "measure_words.sqlite3";
    static final int DB_VERSION = 1;

    // DB constants
    static final String NOUN_TABLE = "nouns";
    static final String NOUN_ID = "_id";
    static final String NOUN_HANZI = "hanzi";
    static final String NOUN_PINYIN = "pinyin";
    static final String NOUN_ENGLISH = "english";

    static final String MEASURE_WORD_TABLE = "measure_words";
    static final String MEASURE_WORD_ID = "_id";
    static final String MEASURE_WORD_HANZI = "hanzi";
    static final String MEASURE_WORD_PINYIN = "pinyin";
    static final String MEASURE_WORD_ENGLISH = "english";

    static final String JOIN_TABLE = "words_measure_words";
    static final String JOIN_ID = "_id";
    static final String JOIN_WORDS_ID = "words_id";
    static final String JOIN_MW_ID = "measure_words_id";
    static final String JOIN_CORRECT = "correct";
    static final String JOIN_INCORRECT = "incorrect";

    public static boolean databaseExists(Context context) {
        return (new File(DB_PATH).exists())
                && (new File(DB_PATH + DB_NAME).exists());
    }

    public static void createDatabaseIfNotExists(Context context)
            throws IOException {

        File dbDir = new File(DB_PATH);
        File dbFile = new File(DB_PATH + DB_NAME);
        if (!dbDir.exists()) {
            Log.v(TAG, "creating db directory");
            dbDir.mkdir();
        }
        if (!dbFile.exists()) {

            Log.v(TAG, "creating db file");
            // Open your local db as the input stream
            InputStream myInput = context.getAssets().open(DB_NAME);

            // Open the empty db as the output stream
            // dbFile.createNewFile();
            OutputStream myOutput = new FileOutputStream(dbFile);

            // transfer bytes from the input to the output
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            // Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }
    }

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.v(TAG, "constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(TAG, "onCreate");
        // anything required here?
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "onUpgrade oldVersion: " + oldVersion + "; newVersion: "
                + newVersion);
        // TODO Fill this out as migrations are necessary
    }
}
