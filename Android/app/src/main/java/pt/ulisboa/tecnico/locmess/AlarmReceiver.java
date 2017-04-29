package pt.ulisboa.tecnico.locmess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import pt.ulisboa.tecnico.locmess.data.entities.PointEntity;

/**
 * Created by goncalo on 28-04-2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final double aggregateDistance = 10;
    private static final String LOG_TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // aggregate everything! :D

        Cursor c = PointEntity.getAllPaths(context);
        while (c.moveToNext()) {
            PointEntity p = new PointEntity(c, context);
            Log.d(LOG_TAG, "Aggregating path " + p.getPoint().toString());
            p.getPoint().aggregatePoints(aggregateDistance);
            Log.d(LOG_TAG, "Resulting path " + p.getPoint().toString());
            p.savePath(context);
        }

        c.close();
    }
}
