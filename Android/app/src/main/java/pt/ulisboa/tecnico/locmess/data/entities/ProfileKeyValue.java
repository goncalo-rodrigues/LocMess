package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;

/**
 * Created by goncalo on 24-03-2017.
 */

public class ProfileKeyValue {
    private String key;
    private String value;

    public ProfileKeyValue(Cursor cursor) {
        int key_idx = cursor.getColumnIndexOrThrow(LocmessContract.ProfileKeyValue.COLUMN_NAME_KEY);
        int val_idx = cursor.getColumnIndexOrThrow(LocmessContract.ProfileKeyValue.COLUMN_NAME_VALUE);
        this.key = cursor.getString(key_idx);
        this.value = cursor.getString(val_idx);
    }

    public ProfileKeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public void save(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocmessContract.ProfileKeyValue.COLUMN_NAME_KEY, getKey());
        values.put(LocmessContract.ProfileKeyValue.COLUMN_NAME_VALUE, getValue());
        db.insert(LocmessContract.ProfileKeyValue.TABLE_NAME, null, values);
        db.close();
    }

    public static Cursor getAll(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor result =  db.query(LocmessContract.ProfileKeyValue.TABLE_NAME, null, null, null, null, null, null);
        //db.close();
        return  result;
    }

    public void delete(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(LocmessContract.ProfileKeyValue.TABLE_NAME,
                LocmessContract.ProfileKeyValue.COLUMN_NAME_KEY + " = ? and " +
                        LocmessContract.ProfileKeyValue.COLUMN_NAME_VALUE + " = ?",
                new String[] {getKey(), getValue()});
        db.close();
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
}
