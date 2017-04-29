package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;
import pt.ulisboa.tecnico.locmess.data.Point;

/**
 * Created by goncalo on 27-04-2017.
 */

public class PointEntity {
    private static final String LOG_TAG = PointEntity.class.getSimpleName();
    private Point point;
    private Date timestamp;
    private long id = -1; // will only be populated if it was retrieved from the db or saved to the db

    public PointEntity(Point point, Date timestamp) {
        this.point = point;
        this.timestamp = timestamp;
    }

    public PointEntity(Point point) {
        this(point, new Date());
    }

    // should instantiate the class using info from db
    public PointEntity(Cursor cursor, Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();

        int x_idx = cursor.getColumnIndexOrThrow(LocmessContract.PointTable.COLUMN_NAME_X);
        int y_idx = cursor.getColumnIndexOrThrow(LocmessContract.PointTable.COLUMN_NAME_Y);
        int ts_idx = cursor.getColumnIndexOrThrow(LocmessContract.PointTable.COLUMN_NAME_TIMESTAMP);
        int next_idx = cursor.getColumnIndexOrThrow(LocmessContract.PointTable.COLUMN_NAME_NEXT);
        int id_idx = cursor.getColumnIndexOrThrow(LocmessContract.PointTable._ID);

        long current_id = cursor.getLong(next_idx);
        this.id = cursor.getLong(id_idx);
        this.timestamp = new Date(cursor.getLong(ts_idx));
        this.point = new Point(cursor.getDouble(x_idx), cursor.getDouble(y_idx));
        Point currentPoint = this.point;
        while (current_id > 0) {
            Cursor c = db.query(LocmessContract.PointTable.TABLE_NAME, null,
                    LocmessContract.PointTable._ID + "=?",
                    new String[] {String.valueOf(current_id)},
                    null, null, null);
            if (!c.moveToNext()) {
                Log.d(LOG_TAG, "Failed to retreive point from DB");
                return;
            }
            current_id = c.isNull(next_idx)? -1 : c.getLong(next_idx);
            currentPoint.nextPoint = new Point(c.getDouble(x_idx), c.getDouble(y_idx));
            currentPoint = currentPoint.nextPoint;
            c.close();
        }

        db.close();
    }

    // should return all points which correspond to the origin of a path
    public static Cursor getAllPaths(Context ctx) {
        String query = "SELECT * FROM " + LocmessContract.PointTable.TABLE_NAME +
                " WHERE " + LocmessContract.PointTable._ID + " NOT IN \n" +
                "(SELECT DISTINCT " + LocmessContract.PointTable.COLUMN_NAME_NEXT +
                " FROM " + LocmessContract.PointTable.TABLE_NAME +
                " WHERE " + LocmessContract.PointTable.COLUMN_NAME_NEXT + " > 0 )";
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        return c;

    }

    public static Cursor getAll(Context ctx) {
        String query = "SELECT * FROM " + LocmessContract.PointTable.TABLE_NAME;
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        return c;
    }

    public void savePath(Context ctx) {
        if (point == null) {
            return;
        }

        if (id >= 0) {
            deletePath(ctx, false);
        }

        Log.d(LOG_TAG, "Saving path...");
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();

            ArrayList<ContentValues> allValues = new ArrayList<>();
            Point current = point;
            do {
                ContentValues values = new ContentValues();
                values.put(LocmessContract.PointTable.COLUMN_NAME_X, current.x);
                values.put(LocmessContract.PointTable.COLUMN_NAME_Y, current.y);
                values.put(LocmessContract.PointTable.COLUMN_NAME_TIMESTAMP, timestamp.getTime());
                values.put(LocmessContract.PointTable.COLUMN_NAME_NEXT, (Integer) null);
                allValues.add(values);
            } while ((current = current.nextPoint) != null);

            long last_insert_id = db.insert(LocmessContract.PointTable.TABLE_NAME, null, allValues.get(allValues.size()-1));

            for (int i = allValues.size() - 2; i >=0; i--) {
                ContentValues values = allValues.get(i);
                values.put(LocmessContract.PointTable.COLUMN_NAME_NEXT, last_insert_id);
                if (i>0 || this.id <= 0)
                    last_insert_id = db.insert(LocmessContract.PointTable.TABLE_NAME, null, values);
                else
                    db.update(LocmessContract.PointTable.TABLE_NAME,
                            values,
                            LocmessContract.PointTable._ID + "=?",
                            new String[] {String.valueOf(this.id)}
                            );
            }

            db.setTransactionSuccessful();

            if (this.id <= 0)
                this.id = last_insert_id;
        } finally {

            db.endTransaction();
            db.close();
        }

    }

    public long save(Context ctx, long prev) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();


        // save the new point
        ContentValues values = new ContentValues();
        values.put(LocmessContract.PointTable.COLUMN_NAME_X, point.x);
        values.put(LocmessContract.PointTable.COLUMN_NAME_Y, point.y);
        values.put(LocmessContract.PointTable.COLUMN_NAME_TIMESTAMP, timestamp.getTime());
        values.put(LocmessContract.PointTable.COLUMN_NAME_NEXT, (Integer) null);
        long last_inserted_id = db.insert(LocmessContract.PointTable.TABLE_NAME, null, values);

        if (prev >= 0) {
            // make the previous point to the new one
            values = new ContentValues();
            values.put(LocmessContract.PointTable.COLUMN_NAME_NEXT, last_inserted_id);
            db.update(LocmessContract.PointTable.TABLE_NAME, values, LocmessContract.PointTable._ID + "=?",
                    new String[] {String.valueOf(prev)} );
        }

        db.close();
        this.id = last_inserted_id;
        return last_inserted_id;
    }

    public void deletePath(Context ctx) {
        deletePath(ctx, true);
    }
    public void deletePath(Context ctx, boolean deleteOrigin) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();

        Log.d(LOG_TAG, "Deleting path...");
        if (id == -1) {
            throw new RuntimeException("You cannot delete this path because it wasn't saved to the db");
        }
        long currentId = id;
        while (currentId >= 0) {
            Cursor c = db.query(LocmessContract.PointTable.TABLE_NAME,
                    new String[] {LocmessContract.PointTable.COLUMN_NAME_NEXT},
                    LocmessContract.PointTable._ID + "=?",
                    new String[] {String.valueOf(currentId)},
                    null, null, null);
            if (!c.moveToNext()) {
                Log.d(LOG_TAG, "Trying to delete a point that didn't exist!");
                break;
            }

            if (currentId != id || deleteOrigin) {
                db.delete(LocmessContract.PointTable.TABLE_NAME, LocmessContract.PointTable._ID + "=?",
                        new String[] {String.valueOf(currentId)});
            }


            int next_idx = c.getColumnIndexOrThrow(LocmessContract.PointTable.COLUMN_NAME_NEXT);

            currentId = c.isNull(next_idx) ? -1 : c.getLong(next_idx);

            c.close();
        }

        db.close();
        if (deleteOrigin)
            this.id = -1;
    }

    public static void removeAllBefore(Date date, Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();

        int rowsRemoved = db.delete(LocmessContract.PointTable.TABLE_NAME,
                LocmessContract.PointTable.COLUMN_NAME_TIMESTAMP + " <= ?",
                new String[] {String.valueOf(date.getTime())});
        db.close();
        Log.d(LOG_TAG, "Removed " + rowsRemoved);
    }

    public Point getPoint() {
        return point;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
