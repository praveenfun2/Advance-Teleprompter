package com.praveengupta.capstone_project.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by Praveen Gupta on 9/4/2016.
 */
public class MyProvider extends ContentProvider {

    MySqlHelper mySqlHelper;
    public static UriMatcher uriMatcher;
    public static final int FILEINFOS = 1;
    public static final int FILEINFO = 2;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Contracts.AUTHORITY, Contracts.fileInfo.TABLE_NAME, FILEINFOS);
        uriMatcher.addURI(Contracts.AUTHORITY, Contracts.fileInfo.TABLE_NAME + "/#", FILEINFO);
    }

    @Override
    public boolean onCreate() {
        mySqlHelper = MySqlHelper.getInstance(getContext(), "myDatabase", null, 1);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {

        SQLiteDatabase sqLiteDatabase = mySqlHelper.getReadableDatabase();
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case FILEINFOS: {
                sqLiteQueryBuilder.setTables(Contracts.fileInfo.TABLE_NAME);
                break;
            }
            case FILEINFO: {
                sqLiteQueryBuilder.setTables(Contracts.fileInfo.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(Contracts.fileInfo._ID + " = " + uri.getLastPathSegment());
                break;
            }
            default:
                throw new IllegalArgumentException("Not even matched a single table/record");
        }
        Cursor cursor = sqLiteQueryBuilder.query(sqLiteDatabase, strings, s, strings1, s1, null, Contracts.fileInfo.FILE_NAME + " ASC", null);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)) {
            case FILEINFOS:
                return Contracts.fileInfo.CONTENT_TYPE;
            case FILEINFO:
                return Contracts.fileInfo.CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        switch (uriMatcher.match(uri)) {
            case FILEINFOS: {
                SQLiteDatabase sqLiteDatabase = mySqlHelper.getWritableDatabase();
                long id = sqLiteDatabase.insert(Contracts.fileInfo.TABLE_NAME, null, contentValues);
                return getUriForId(id, uri);
            }
            default:
                throw new IllegalArgumentException("Please use some valid uri..." + uri.toString());

        }
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {

        SQLiteDatabase sqLiteDatabase = mySqlHelper.getWritableDatabase();
        int delCount = 0;
        switch (uriMatcher.match(uri)) {
            case FILEINFOS: {
                delCount = sqLiteDatabase.delete(Contracts.fileInfo.TABLE_NAME, s, strings);
                break;
            }
            case FILEINFO: {
                String where = Contracts.fileInfo._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(s)) where += " AND " + s;
                delCount = sqLiteDatabase.delete(Contracts.fileInfo.TABLE_NAME, where, strings);
                break;
            }
            default:
                throw new IllegalArgumentException("Invalid uri to delete");
        }
        if (delCount > 0) getContext().getContentResolver().notifyChange(uri, null);
        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {

        SQLiteDatabase sqLiteDatabase = mySqlHelper.getWritableDatabase();
        int updateCount = 0;
        switch (uriMatcher.match(uri)) {
            case FILEINFOS: {
                updateCount = sqLiteDatabase.update(Contracts.fileInfo.TABLE_NAME, contentValues, s, strings);
                break;
            }
            case FILEINFO: {
                String where = Contracts.fileInfo._ID + " = " + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(s)) where += " AND " + s;
                updateCount = sqLiteDatabase.update(Contracts.fileInfo.TABLE_NAME, contentValues, where, strings);
                break;
            }
            default:
                throw new IllegalArgumentException("Invalid uri to update");
        }
        if (updateCount > 0) getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }


    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            //if (!isInBatchMode()) {
            // notify all listeners of changes:
            getContext().
                    getContentResolver().
                    notifyChange(itemUri, null);
            //}
            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException(
                "Problem while inserting into uri: " + uri);
    }


   public static final class Contracts {
        static final String AUTHORITY = "com.praveengupta.capstone_project";
        static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

        public static class fileInfo implements BaseColumns {
            static final String TABLE_NAME = "fileInfos";
            public static final String FILE_NAME = "file_name";
            static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + TABLE_NAME;
            static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/fileInfo";
            public static final String FILE_PATH = "address";
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Contracts.CONTENT_URI, TABLE_NAME);
        }

    }
}