package com.higheranimals.measurewords;

import java.io.IOException;

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
        String NOUN_ID = "_n_id";
        String NOUN_HANZI = "n_hanzi";
        String NOUN_PINYIN = "n_pinyin";
        String NOUN_ENGLISH = "n_english";
        String MEASURE_WORD_ID = "_mw_id";
        String MEASURE_WORD_HANZI = "mw_hanzi";
        String MEASURE_WORD_PINYIN = "mw_pinyin";
        String MEASURE_WORD_ENGLISH = "mw_english";
        String CORRECT = "nmw_correct";
        String INCORRECT = "nmw_incorrect";
        String QUESTION_ID = "_nmw_id";
    }

    interface Ordering {
        String RANDOM = "RANDOM()";
        String HARDEST = Field.INCORRECT + " - " + Field.CORRECT
                + " DESC, RANDOM()";
    }

    interface UriParameter {
        String LIMIT = "limit";
        String DISTINCT = "distinct";
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
        qb.setTables("nouns LEFT OUTER JOIN nouns_measure_words ON "
                + Field.NOUN_ID
                + " = nouns_measure_words.nmw_noun_id INNER JOIN measure_words on nouns_measure_words.nmw_measure_word_id = "
                + Field.MEASURE_WORD_ID);
        return qb;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = createQueryBuilder();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int id = getId(uri);
        if (id < 0) {
            if (uri.getQueryParameter(UriParameter.DISTINCT) != null) {
                qb.setDistinct(true);
            }
            return qb.query(db, projection, selection, selectionArgs, null,
                    null, sortOrder, uri.getQueryParameter(UriParameter.LIMIT));
        } else {
            return qb.query(db, projection, Field.QUESTION_ID + " = " + id,
                    null, null, null, null);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        /*
         * SQLiteDatabase db = dbHelper.getReadableDatabase(); int id =
         * getId(uri);
         */
        return 0;
    }

}
