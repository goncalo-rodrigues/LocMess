package pt.ulisboa.tecnico.locmess.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by goncalo on 23-03-2017.
 */

public class LocmessDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Locmess.db";

    public LocmessDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(LocmessContract.SQL_CREATE_1);
        db.execSQL(LocmessContract.SQL_CREATE_2);
        db.execSQL(LocmessContract.SQL_CREATE_3);
        db.execSQL(LocmessContract.SQL_CREATE_4);
        db.execSQL(LocmessContract.SQL_CREATE_5);
        db.execSQL(LocmessContract.SQL_CREATE_6);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(LocmessContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
