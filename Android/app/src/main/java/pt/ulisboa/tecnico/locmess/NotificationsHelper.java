package pt.ulisboa.tecnico.locmess;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by goncalo on 12-04-2017.
 */


public class NotificationsHelper {
    static int notificationCount = 0;
    public static int  startNewMessageNotification(Context context){
        int notificationId = 1;

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(ns);

        //the intent that is started when the notification is clicked
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("notification", true);
//        notificationIntent.putExtra(Constants.NOTIFICATION_ID_EXTRA,notificationId);
//        notificationIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, connection);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context, notificationId,
                notificationIntent, 0);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentText("click to see message")
                .setContentTitle("new message")
                .setTicker("new message")
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingNotificationIntent)
                .setAutoCancel(true);


        Notification notification = notificationBuilder.build();
        notificationManager.notify(notificationId, notification);
        return notificationId;
    }
}
