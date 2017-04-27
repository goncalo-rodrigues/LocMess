package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;

/**
 * Created by goncalo on 23-03-2017.
 */

public class MuleMessageFilter {
    private String messageId;
    private String key;
    private String value;
    private boolean blackList;

    public MuleMessageFilter(String messageId, String key, String value, boolean blackList) {
        init(messageId, key, value, blackList);
    }

    public MuleMessageFilter(Cursor cursor) {
        int id_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageFilter.COLUMN_NAME_MESSAGEID);
        int key_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageFilter.COLUMN_NAME_KEY);
        int val_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageFilter.COLUMN_NAME_VALUE);
        int black_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageFilter.COLUMN_NAME_BLACKLISTED);
        init(cursor.getString(id_idx), cursor.getString(key_idx), cursor.getString(val_idx), cursor.getInt(black_idx) > 0);
    }
    public MuleMessageFilter(JsonReader reader) throws IOException {
        String messageId = null;
        String key = null;
        String value = null;
        boolean blackList = false;


        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "key":
                    key = reader.nextString();
                    break;
                case "value":
                    value = reader.nextString();
                    break;
                case "blacklist":
                    blackList = reader.nextBoolean();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        init(null, key, value, blackList);
    }

    private void init(String messageId, String key, String value, boolean blackList) {
        this.messageId = messageId;
        this.key = key;
        this.value = value;
        this.blackList = blackList;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isBlackList() {
        return blackList;
    }

    public void setBlackList(boolean blackList) {
        this.blackList = blackList;
    }

    public void save(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocmessContract.MessageFilter.COLUMN_NAME_KEY, getKey());
        values.put(LocmessContract.MessageFilter.COLUMN_NAME_VALUE, getValue());
        values.put(LocmessContract.MessageFilter.COLUMN_NAME_BLACKLISTED, isBlackList());
        values.put(LocmessContract.MessageFilter.COLUMN_NAME_MESSAGEID, getMessageId());
        db.insert(LocmessContract.MessageFilter.TABLE_NAME, null, values);
    }

    public void delete(Context ctx) {
        
    }

    public JSONObject getJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("key", getKey());
            result.put("value", getValue());
            result.put("blacklist", isBlackList());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
