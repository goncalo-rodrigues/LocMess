package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;

/**
 * Created by goncalo on 23-03-2017.
 */

public class MuleMessage extends Message {
    private List<MuleMessageFilter> filters;
    private int hops = 0;

    public MuleMessage(int id, String messageText, String author, String location, Date startDate, Date endDate, List<MuleMessageFilter> filters, int hops) {
        super(id, messageText, author, location, startDate, endDate);
        this.filters = filters;
        this.hops = hops;
    }

    public MuleMessage(final Cursor cursor) {
        super(cursor);
        int hops_idx = cursor.getColumnIndexOrThrow(LocmessContract.MuleMessageTable.COLUMN_NAME_HOPS);
        this.hops = cursor.getInt(hops_idx);
    }

    public List<MuleMessageFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<MuleMessageFilter> filters) {
        this.filters = filters;
    }

    public int getHops() {
        return hops;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }

    @Override
    public void save(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocmessContract.MuleMessageTable.COLUMN_NAME_CONTENT, getMessageText());
        values.put(LocmessContract.MuleMessageTable.COLUMN_NAME_AUTHOR, getAuthor());
        values.put(LocmessContract.MuleMessageTable.COLUMN_NAME_ID, getId());
        values.put(LocmessContract.MuleMessageTable.COLUMN_NAME_STARTDATE, getStartDate().toString());
        values.put(LocmessContract.MuleMessageTable.COLUMN_NAME_ENDDATE, getEndDate().toString());
        values.put(LocmessContract.MuleMessageTable.COLUMN_NAME_LOCATION, getLocation());
        values.put(LocmessContract.MuleMessageTable.COLUMN_NAME_HOPS, getHops());
        db.insert(LocmessContract.MuleMessageTable.TABLE_NAME, null, values);
        // TODO : save filters
    }
}
