package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;

/**
 * Created by goncalo on 23-03-2017.
 */

public class MuleMessageFilter {
    private int messageId;
    private String key;
    private String value;
    private boolean blackList;

    public MuleMessageFilter(int messageId, String key, String value, boolean blackList) {
        init(messageId, key, value, blackList);
    }

    public MuleMessageFilter(Cursor cursor) {
        int id_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageFilter.COLUMN_NAME_MESSAGEID);
        int key_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageFilter.COLUMN_NAME_KEY);
        int val_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageFilter.COLUMN_NAME_VALUE);
        int black_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageFilter.COLUMN_NAME_BLACKLISTED);
        init(cursor.getInt(id_idx), cursor.getString(key_idx), cursor.getString(val_idx), cursor.getInt(black_idx) > 0);
    }

    private void init(int messageId, String key, String value, boolean blackList) {
        this.messageId = messageId;
        this.key = key;
        this.value = value;
        this.blackList = blackList;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
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
        db.insert(LocmessContract.MessageTable.TABLE_NAME, null, values);
    }

    public void delete(Context ctx) {
        
    }
}
