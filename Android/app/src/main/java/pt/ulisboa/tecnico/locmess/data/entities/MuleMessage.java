package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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

    public MuleMessage(String id, String messageText, String author, String location, Date startDate, Date endDate, List<MuleMessageFilter> filters, int hops) {
        init(id, messageText, author, location, startDate, endDate, filters, hops);
    }

    public MuleMessage(JsonReader reader) throws IOException {
        String id = null;
        String messageText = null;
        String author = null;
        String location = null;
        Date startDate = null;
        Date endDate = null;
        List<MuleMessageFilter> filters = new ArrayList<>();
        int hops = 0;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "id":
                    id = reader.nextString();
                    break;
                case "messageText":
                    messageText = reader.nextString();
                    break;
                case "author":
                    author = reader.nextString();
                    break;
                case "location":
                    location = reader.nextString();
                    break;
                case "startDate":
                    startDate = new Date(reader.nextString());
                    break;
                case "endDate":
                    endDate = new Date(reader.nextString());
                    break;
                case "hops":
                    hops = reader.nextInt();
                    break;
                case "filters":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        filters.add(new MuleMessageFilter(reader));
                    }
                    reader.endArray();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        init(id, messageText, author, location, startDate, endDate, filters, hops);
    }

    public MuleMessage(final Cursor cursor) {
        super(cursor);
        int hops_idx = cursor.getColumnIndexOrThrow(LocmessContract.MuleMessageTable.COLUMN_NAME_HOPS);
        this.hops = cursor.getInt(hops_idx);
    }

    protected void init(String id, String messageText, String author, String location, Date startDate, Date endDate, List<MuleMessageFilter> filters, int hops) {
        super.init(id, messageText, author, location, startDate, endDate);
        this.filters = filters;
        this.hops = hops;
    }



    // if context is not null, this method will retrieve the filters from the DB
    public List<MuleMessageFilter> getFilters(@Nullable Context ctx) {
        if (filters == null) {
            if (ctx != null) {
                LocmessDbHelper helper = new LocmessDbHelper(ctx);
                SQLiteDatabase db = helper.getReadableDatabase();
                Cursor result =  db.query(LocmessContract.MuleMessageTable.TABLE_NAME, null,
                        LocmessContract.MessageFilter.COLUMN_NAME_MESSAGEID + " = ?",
                        new String[] {String.valueOf(getId())}, null, null, null);
                db.close();
                filters = new ArrayList<>();
                for(result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
                    // The Cursor is now set to the right position
                    filters.add(new MuleMessageFilter(result));
                }
            } else {
                filters = new ArrayList<>();
            }

        }
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
        for (MuleMessageFilter f : getFilters(null)) {
            f.save(ctx);
        }
        db.close();
    }

    public static Cursor getAll(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor result =  db.query(LocmessContract.MuleMessageTable.TABLE_NAME, null, null, null, null, null, LocmessContract.MessageTable.COLUMN_NAME_ID);
        //db.close();
        return  result;
    }

    @Override
    public void delete(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(LocmessContract.CreatedMessageTable.TABLE_NAME,
                LocmessContract.CreatedMessageTable.COLUMN_NAME_ID + " = ?",
                new String[] {getId()});
        // TODO: delete filters
        db.close();
    }

    public JSONObject getJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("id", getId());
            result.put("messageText", getMessageText());
            result.put("author", getAuthor());
            result.put("location", getLocation());
            result.put("startDate", getStartDate().toString());
            result.put("endDate", getEndDate().toString());
            result.put("hops", getHops());
            JSONArray filts = new JSONArray();
            for (MuleMessageFilter f : filters) {
                filts.put(f.getJson());
            }
            result.put("filters", filts);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }



}
