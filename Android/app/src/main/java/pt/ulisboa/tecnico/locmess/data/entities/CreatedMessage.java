package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;

/**
 * Created by goncalo on 24-03-2017.
 */

public class CreatedMessage extends Message {
    public CreatedMessage(String id, String messageText, String author, String location, Date startDate, Date endDate) {
        super(id, messageText, author, location, startDate, endDate);
    }

    public CreatedMessage(final Cursor cursor) {
        super(cursor);
    }

    @Override
    public void save(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocmessContract.CreatedMessageTable.COLUMN_NAME_CONTENT, getMessageText());
        values.put(LocmessContract.CreatedMessageTable.COLUMN_NAME_AUTHOR, getAuthor());
        values.put(LocmessContract.CreatedMessageTable.COLUMN_NAME_ID, getId());
        values.put(LocmessContract.CreatedMessageTable.COLUMN_NAME_STARTDATE, getStartDate().toString());
        values.put(LocmessContract.CreatedMessageTable.COLUMN_NAME_ENDDATE, getEndDate().toString());
        values.put(LocmessContract.CreatedMessageTable.COLUMN_NAME_LOCATION, getLocation());
        db.insert(LocmessContract.CreatedMessageTable.TABLE_NAME, null, values);
    }

    @Override
    public void delete(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(LocmessContract.CreatedMessageTable.TABLE_NAME,
                LocmessContract.CreatedMessageTable.COLUMN_NAME_ID + " = ?",
                new String[] {getId()});
        db.close();
    }

    public static Cursor getAll(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor result = db.query(LocmessContract.CreatedMessageTable.TABLE_NAME, null, null, null, null, null, LocmessContract.CreatedMessageTable.COLUMN_NAME_ID);
        //db.close();
        return result;
    }
}
