package pt.ulisboa.tecnico.locmess.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by goncalo on 23-03-2017.
 */

public class LocmessDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "Locmess.db";

    public LocmessDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(LocmessContract.SQL_CREATE_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_CREATED_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_MULE_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_MESSAGE_FILTER_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_LOCATION_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_PROFILE_KEYVAL_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_FULL_LOCATION_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_SSIDS_CACHE_TBL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(LocmessContract.SQL_DELETE_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_MESSAGE_FILTER_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_CREATED_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_MULE_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_LOCATION_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_PROFILE_KEYVAL_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_FULL_LOCATION_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_SSIDS_CACHE_TBL);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
