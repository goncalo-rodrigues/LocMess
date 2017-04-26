package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;

/**
 * Created by goncalo on 26-04-2017.
 */

public class SSIDSCache {
    public static ArrayList<String> getAll(Context ctx) {
        ArrayList<String> ssids = new ArrayList<>();
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor result =  db.query(LocmessContract.SSIDSCacheTable.TABLE_NAME, null, null, null, null, null, null);
        int name_idx = result.getColumnIndex(LocmessContract.SSIDSCacheTable.COLUMN_NAME_NAME);
        while (result.moveToNext()) {
            ssids.add(result.getString(name_idx));
        }
        result.close();
        db.close();
        return ssids;
    }

    public static boolean exists(String name, Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor result =  db.query(LocmessContract.SSIDSCacheTable.TABLE_NAME, null,
                LocmessContract.SSIDSCacheTable.COLUMN_NAME_NAME + " = ?", new String[] {name},
                null, null, null);

        boolean exists = result.getCount() > 0;
        result.close();
        db.close();
        return exists;
    }

    public static boolean existsAtLeastOne(List<String> names, Context ctx) {
        if (names.size() == 0) return false;
        // little hack hehe.
        String where_clause = new String(new char[names.size()]).replace("\0",
                LocmessContract.SSIDSCacheTable.COLUMN_NAME_NAME + " = ? OR ");
        where_clause = where_clause.substring(0, where_clause.length() - 3);
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor result =  db.query(LocmessContract.SSIDSCacheTable.TABLE_NAME, null,
                where_clause, names.toArray(new String[0]),
                null, null, null);

        boolean exists = result.getCount()  > 0;
        result.close();
        db.close();
        return exists;
    }

    public static void insertOrUpdate(String name, Context ctx) {
        Date date = new Date();
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocmessContract.SSIDSCacheTable.COLUMN_NAME_NAME, name);
        values.put(LocmessContract.SSIDSCacheTable.COLUMN_NAME_LAST_SEEN, date.getTime());
        if (exists(name, ctx)) {
            db.update(LocmessContract.SSIDSCacheTable.TABLE_NAME, values,
                    LocmessContract.SSIDSCacheTable.COLUMN_NAME_NAME + " = ?", new String[] {name});
        } else {
            db.insert(LocmessContract.SSIDSCacheTable.TABLE_NAME, null, values);
        }
        db.close();
    }

    public static void insertOrUpdate(List<String> names, Context ctx) {
        for (String name: names) {
            insertOrUpdate(name, ctx);
        }
    }


    public static void removeAllBefore(Date date, Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete(LocmessContract.SSIDSCacheTable.TABLE_NAME,
                LocmessContract.SSIDSCacheTable.COLUMN_NAME_LAST_SEEN + " <= ?",
                new String[] {String.valueOf(date.getTime())});
        db.close();
    }

}
