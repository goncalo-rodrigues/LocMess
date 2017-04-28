package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by goncalo on 23-03-2017.
 */

public class MuleMessage extends Message {
    private static final String LOG_TAG = MuleMessage.class.getSimpleName();
    private List<MuleMessageFilter> filters;
    private int hops = 0;
    private FullLocation fullLocation;
    public static final int MAX_MULE_MESSAGES = 16;

    public MuleMessage(String id, String messageText, String author, FullLocation location, Date startDate, Date endDate, List<MuleMessageFilter> filters, int hops) {
        init(id, messageText, author, location, startDate, endDate, filters, hops);
    }

    @Override
    public String toString() {
        return getJson().toString();
    }

    public MuleMessage(JsonReader reader) throws IOException {
        String id = null;
        String messageText = null;
        String author = null;
        FullLocation location = null;
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
                    location = new FullLocation(reader);
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

        for (MuleMessageFilter f: filters) {
            f.setMessageId(id);
        }
        init(id, messageText, author, location, startDate, endDate, filters, hops);
    }

    public MuleMessage(final Cursor cursor, Context ctx) {
        super(cursor);
        int hops_idx = cursor.getColumnIndexOrThrow(LocmessContract.MuleMessageTable.COLUMN_NAME_HOPS);
        this.hops = cursor.getInt(hops_idx);
        this.getFullLocation(ctx);
        this.getFilters(ctx);
    }

    protected void init(String id, String messageText, String author, FullLocation location, Date startDate, Date endDate, List<MuleMessageFilter> filters, int hops) {
        super.init(id, messageText, author, location.getLocation(), startDate, endDate, false);
        this.filters = filters;
        this.hops = hops;
        this.fullLocation = location;
    }



    // if context is not null, this method will retrieve the filters from the DB
    public List<MuleMessageFilter> getFilters(@Nullable Context ctx) {
        if (filters == null) {
            if (ctx != null) {
                LocmessDbHelper helper = new LocmessDbHelper(ctx);
                SQLiteDatabase db = helper.getReadableDatabase();
                Cursor result =  db.query(LocmessContract.MessageFilter.TABLE_NAME, null,
                        LocmessContract.MessageFilter.COLUMN_NAME_MESSAGEID + " = ?",
                        new String[] {getId()}, null, null, null);

                filters = new ArrayList<>();
                for(result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
                    // The Cursor is now set to the right position
                    filters.add(new MuleMessageFilter(result));
                }
                db.close();
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
        if (fullLocation != null)
            fullLocation.save(ctx);

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
        values.put(LocmessContract.MuleMessageTable.COLUMN_NAME_TIMESTAMP, (new Date()).getTime());
        try {
            db.insert(LocmessContract.MuleMessageTable.TABLE_NAME, null, values);
        } catch (SQLiteConstraintException e) {
            Log.e(LOG_TAG, e.toString());
        }

        for (MuleMessageFilter f : getFilters(null)) {
            f.save(ctx);
        }

        deleteUseless(ctx);
        Cursor query = db.query(LocmessContract.MuleMessageTable.TABLE_NAME, null, LocmessContract.MuleMessageTable.COLUMN_NAME_HOPS + " > 0",
                null, null, null, LocmessContract.MuleMessageTable.COLUMN_NAME_TIMESTAMP);
        int count = query.getCount();
        db.beginTransaction();
        while (count > MAX_MULE_MESSAGES) {
            query.moveToNext();
            String id = query.getString(query.getColumnIndex(LocmessContract.MuleMessageTable.COLUMN_NAME_ID));
            db.delete(LocmessContract.MuleMessageTable.TABLE_NAME, LocmessContract.MuleMessageTable.COLUMN_NAME_ID + " = ?", new String[] {id});
            Log.d(LOG_TAG, "Deleted message due to overcrowding " +  id);
            count--;

        }
        db.endTransaction();
        query.close();
        db.close();


    }

    public static void deleteUseless(Context ctx) {
        Cursor c = getAll(ctx, LocmessContract.MuleMessageTable.COLUMN_NAME_TIMESTAMP);
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        int endDateIndex = c.getColumnIndex(LocmessContract.MuleMessageTable.COLUMN_NAME_ENDDATE);
        int idIndex = c.getColumnIndex(LocmessContract.MuleMessageTable.COLUMN_NAME_ID);
        while (c.moveToNext()) {
            Date endDate = new Date(c.getString(endDateIndex));
            if (endDate.before(new Date())) {
                String id = c.getString(idIndex);
                db.delete(LocmessContract.MuleMessageTable.TABLE_NAME, LocmessContract.MuleMessageTable.COLUMN_NAME_ID + " = ?", new String[] {id});
                Log.d(LOG_TAG, "Deleted useless message " +  id + " enddate: " + endDate.toString());
            }
        }
    }

    public static Cursor getAll(Context ctx) {
        return getAll(ctx, LocmessContract.MuleMessageTable.COLUMN_NAME_TIMESTAMP);
    }

    private static Cursor getAll(Context ctx, String orderBy) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor result =  db.query(LocmessContract.MuleMessageTable.TABLE_NAME, null, null, null, null, null, orderBy);
        //db.close();
        return  result;
    }

    @Override
    public void delete(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(LocmessContract.MuleMessageTable.TABLE_NAME,
                LocmessContract.MuleMessageTable.COLUMN_NAME_ID + " = ?",
                new String[] {getId()});
        db.delete(LocmessContract.MessageFilter.TABLE_NAME,
                LocmessContract.MessageFilter.COLUMN_NAME_MESSAGEID + " = ?",
                new String[] {getId()});
        // TODO: delete fullLocation, maybe?
        db.close();
    }

    public JSONObject getJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("id", getId());
            result.put("messageText", getMessageText());
            result.put("author", getAuthor());
            result.put("location", fullLocation.getJson());
            result.put("startDate", getStartDate().toString());
            result.put("endDate", getEndDate().toString());
            result.put("hops", getHops());
            JSONArray filts = new JSONArray();
            for (MuleMessageFilter f : getFilters(null)) {
                filts.put(f.getJson());
            }
            result.put("filters", filts);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


    public FullLocation getFullLocation() {
        return fullLocation;
    }

    public FullLocation getFullLocation(Context ctx) {
        if (fullLocation == null)
            fullLocation = FullLocation.get(ctx, getLocation());
        return fullLocation;
    }

    public ReceivedMessage toReceived() {
        return new ReceivedMessage(getId(), getMessageText(), getAuthor(), getLocation(), getStartDate(), getEndDate(), false);
    }

    public boolean amIallowedToReceiveThisMessage(Context ctx) {
        List<MuleMessageFilter> filters = getFilters(ctx);
        if (getAuthor().equals(((NetworkGlobalState) ctx.getApplicationContext()).getUsername())) return false;
        if (filters.size() == 0) return true;
        Cursor pkvc = ProfileKeyValue.getAll(ctx);
        try {
            while (pkvc.moveToNext()) {
                ProfileKeyValue pkv = new ProfileKeyValue(pkvc);
                for (MuleMessageFilter f: filters) {
                    if (pkv.getKey().equals(f.getKey())) {
                        if (pkv.getValue().equals(f.getValue()) && f.isBlackList()) {
                            return false;
                        } else if (!pkv.getValue().equals(f.getValue()) && !f.isBlackList()) {
                            return false;
                        }
                    }
                }
            }
        } finally {
            pkvc.close();
        }
        return true;
    }

    public boolean existsInDb(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor result =  db.query(LocmessContract.MuleMessageTable.TABLE_NAME, null,
                LocmessContract.MuleMessageTable.COLUMN_NAME_ID + "=?",
                new String[] {getId()}, null, null, null);
        boolean exists = result.getCount() != 0;
        result.close();
        db.close();
        return exists;
    }


}
