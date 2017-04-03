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

public class Location {
    private String location;

    public Location(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void save(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocmessContract.LocationTable.COLUMN_NAME_LOCATION, getLocation());
        db.insert(LocmessContract.LocationTable.TABLE_NAME, null, values);
        db.close();
    }

    public static Cursor getAll(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor result =  db.query(LocmessContract.LocationTable.TABLE_NAME, null, null, null, null, null, null);
        //db.close();
        return  result;
    }

    public void delete(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(LocmessContract.LocationTable.TABLE_NAME,
                LocmessContract.LocationTable.COLUMN_NAME_LOCATION + " = ?",
                new String[] {getLocation()});
        db.close();
    }
}
