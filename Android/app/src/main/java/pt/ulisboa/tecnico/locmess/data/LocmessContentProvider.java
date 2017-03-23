package pt.ulisboa.tecnico.locmess.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.URI;

/**
 * Created by goncalo on 23-03-2017.
 */

public class LocmessContentProvider extends ContentProvider {
    // Creates a UriMatcher object.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // Dbhelper
    private LocmessDbHelper mHelper;

    private static final int GET_MESSAGES_CODE = 1;
    private static final int GET_MESSAGE_CODE = 1;

    static {
        /*
         * The calls to addURI() go here, for all of the content URI patterns that the provider
         * should recognize. For this snippet, only the calls for table 3 are shown.
         */

        sUriMatcher.addURI("pt.ulisboa.tecnico.locmess", LocmessContract.MessageTable.TABLE_NAME, GET_MESSAGES_CODE);
        sUriMatcher.addURI("pt.ulisboa.tecnico.locmess", LocmessContract.MessageTable.TABLE_NAME + "/#", GET_MESSAGE_CODE);
    }
    @Override
    public boolean onCreate() {
        mHelper =  new LocmessDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mHelper.getReadableDatabase();
        String tableName = LocmessContract.MessageTable.TABLE_NAME;
        Cursor result = null;
        result = database.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase database = mHelper.getWritableDatabase();
        String tableName = LocmessContract.MessageTable.TABLE_NAME;
        Uri result = null;
        long row = database.insert(tableName, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri.buildUpon().appendPath(String.valueOf(row)).build();
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
