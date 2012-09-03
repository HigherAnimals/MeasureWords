package com.higheranimals.measurewords;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class QuestionProvider extends ContentProvider {

    private static final String TAG = "QuestionProvider";

    public static final Uri CONTENT_URI = Uri
            .parse("content://com.higheranimals.measurewords.questionprovider");
    public static final String SINGLE_RECORD_MIME_TYPE = "vnd.android.cursor.item/vnd.higheranimals.measurewords.question";
    public static final String MULTIPLE_RECORDS_MIME_TYPE = "vnd.android.cursor.dir/vnd.higheranimals.measurewords.mquestion";
    private DbHelper dbHelper;

    interface Field {
        String NOUN_ID = "noun_id";
        String NOUN_HANZI = "noun_hanzi";
        String NOUN_PINYIN = "noun_pinyin";
        String NOUN_ENGLISH = "noun_english";
        String MEASURE_WORD_ID = "measure_word_id";
        String MEASURE_WORD_HANZI = "measure_word_hanzi";
        String MEASURE_WORD_PINYIN = "measure_word_pinyin";
        String MEASURE_WORD_ENGLISH = "measure_word_english";
        String CORRECT = "nouns_measure_word_correct";
        String INCORRECT = "nouns_measure_word_incorrect";
    }

    interface UriParameter {
        String LIMIT = "limit";
        String DISTINCT = "distinct";
    }

    private static Map<String, String> columnMap;
    static {
        Map<String, String> tmpMap = new HashMap<String, String>();
        tmpMap.put(Field.NOUN_ID, "nouns._id AS " + Field.NOUN_ID);
        tmpMap.put(Field.NOUN_HANZI, "nouns.hanzi AS " + Field.NOUN_HANZI);
        tmpMap.put(Field.NOUN_PINYIN, "nouns.pinyin AS " + Field.NOUN_PINYIN);
        tmpMap.put(Field.NOUN_ENGLISH, "nouns.english AS " + Field.NOUN_ENGLISH);
        tmpMap.put(Field.MEASURE_WORD_ID, "measure_words._id AS "
                + Field.MEASURE_WORD_ID);
        tmpMap.put(Field.MEASURE_WORD_HANZI, "measure_words.hanzi AS "
                + Field.MEASURE_WORD_HANZI);
        tmpMap.put(Field.MEASURE_WORD_PINYIN, "measure_words.pinyin AS "
                + Field.MEASURE_WORD_PINYIN);
        tmpMap.put(Field.MEASURE_WORD_ENGLISH, "measure_words.english AS "
                + Field.MEASURE_WORD_ENGLISH);
        tmpMap.put(Field.CORRECT, "nouns_measure_words.correct AS "
                + Field.CORRECT);
        tmpMap.put(Field.INCORRECT, "nouns_measure_words.incorrect "
                + Field.INCORRECT);
        columnMap = tmpMap;
    }

    public static boolean isReady(Context context) {
        return DbHelper.databaseExists(context);
    }

    public static void initializeDataIfNecessary(Context context)
            throws IOException {
        DbHelper.createDatabaseIfNotExists(context);
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        // No need for this yet.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return getId(uri) < 0 ? MULTIPLE_RECORDS_MIME_TYPE
                : SINGLE_RECORD_MIME_TYPE;
    }

    private int getId(Uri uri) {
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment != null) {
            try {
                return Integer.parseInt(lastPathSegment);
            } catch (NumberFormatException e) {
                Log.v(TAG, e.toString());
            }
        }
        return -1;
    }

    @Override
    public Uri insert(Uri arg0, ContentValues arg1) {
        // No need for this yet
        return null;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return false;
    }

    private static SQLiteQueryBuilder createQueryBuilder() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("nouns LEFT OUTER JOIN nouns_measure_words ON nouns._id = nouns_measure_words.noun_id INNER JOIN measure_words on nouns_measure_words.measure_word_id = measure_words._id");
        qb.setProjectionMap(columnMap);
        return qb;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = createQueryBuilder();
        if (uri.getQueryParameter(UriParameter.DISTINCT) != null) {
            qb.setDistinct(true);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder, uri.getQueryParameter(UriParameter.LIMIT));
        // db.close();
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
