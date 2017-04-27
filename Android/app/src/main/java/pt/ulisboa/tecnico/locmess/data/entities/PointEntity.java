package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.Context;
import android.database.Cursor;

import java.util.Date;

import pt.ulisboa.tecnico.locmess.data.Point;

/**
 * Created by goncalo on 27-04-2017.
 */

public class PointEntity {
    private Point point;
    private Date timestamp;

    public PointEntity(Point point, Date timestamp) {

    }

    public PointEntity(Point point) {
        this(point, new Date());
    }

    // should instantiate the class using info from db
    public PointEntity(Cursor cursor) {
        // TODO
    }

    // should return all points which correspond to the origin of a path
    public static Cursor getAllPaths(Context ctx) {
        // TODO
        return null;
    }

    public void save(Context ctx) {
        // TODO: don't forget to save all points in the path
    }

    public void delete(Context ctx) {
        // TODO: don't forget to delete all points in the path
    }
}
