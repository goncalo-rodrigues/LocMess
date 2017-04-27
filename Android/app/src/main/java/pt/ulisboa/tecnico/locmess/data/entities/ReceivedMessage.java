package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Date;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;

/**
 * Created by goncalo on 24-03-2017.
 */

public class ReceivedMessage extends Message {
    private static final String LOG_TAG = ReceivedMessage.class.getSimpleName();

    public ReceivedMessage(String id, String messageText, String author, String location, Date startDate, Date endDate, boolean centralized) {
        super(id, messageText, author, location, startDate, endDate, centralized);
    }

    public ReceivedMessage(final Cursor cursor) {
        super(cursor);
    }

    @Override
    public void save(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocmessContract.MessageTable.COLUMN_NAME_CONTENT, getMessageText());
        values.put(LocmessContract.MessageTable.COLUMN_NAME_AUTHOR, getAuthor());
        values.put(LocmessContract.MessageTable.COLUMN_NAME_ID, getId());
        values.put(LocmessContract.MessageTable.COLUMN_NAME_STARTDATE, getStartDate().toString());
        values.put(LocmessContract.MessageTable.COLUMN_NAME_ENDDATE, getEndDate().toString());
        values.put(LocmessContract.MessageTable.COLUMN_NAME_LOCATION, getLocation());
        values.put(LocmessContract.MessageTable.COLUMN_NAME_CENTRALIZED, isCentralized());
        values.put(LocmessContract.MessageTable.COLUMN_NAME_TIMESTAMP, (new Date()).getTime());
        try {
            db.insert(LocmessContract.MessageTable.TABLE_NAME, null, values);
        } catch (SQLiteConstraintException e) {
            Log.e(LOG_TAG, e.toString());
        }

        db.close();
    }

    @Override
    public void delete(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(LocmessContract.MessageTable.TABLE_NAME,
                LocmessContract.MessageTable.COLUMN_NAME_ID + " = ?",
                new String[] {getId()});
        db.close();
    }

    public static Cursor getAll(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor result =  db.query(LocmessContract.MessageTable.TABLE_NAME, null, null, null, null, null, LocmessContract.MessageTable.COLUMN_NAME_TIMESTAMP + " DESC");
        //db.close();
        return  result;
    }
}
