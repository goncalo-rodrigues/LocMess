package pt.ulisboa.tecnico.locmess.wifidirect;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.ulisboa.tecnico.locmess.data.Point;
import pt.ulisboa.tecnico.locmess.data.entities.FullLocation;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessage;
import pt.ulisboa.tecnico.locmess.data.entities.PointEntity;
import pt.ulisboa.tecnico.locmess.data.entities.SSIDSCache;

/**
 * Created by goncalo on 10-04-2017.
 */

public class Policy {
    private static final String LOG_TAG = Policy.class.getSimpleName();
    private HashSet<MessageDevicePair> alreadySent = new HashSet<>();
    // returns true if should send message to device
    public boolean shouldSendToPeer(SimWifiP2pDevice device, MuleMessage message) {
        MessageDevicePair mdp = new MessageDevicePair(device.deviceName, message.getId());
        if (alreadySent.contains(mdp)) {
            Log.d(LOG_TAG, "already sent to " + device.deviceName);
            return false;
        } else {
            alreadySent.add(mdp);
            return true;
        }
    }

    public boolean shouldKeepMessage(MuleMessage message, Context ctx) {
        if (message.existsInDb(ctx)) return false;
        FullLocation msgLocation = message.getFullLocation();
        if (msgLocation.isWifi()) {
            if (SSIDSCache.existsAtLeastOne(msgLocation.getSsids(), ctx)) {
                return true;
            } else {
                return false;
            }
        } else {
            Cursor paths = PointEntity.getAllPaths(ctx);
            Point targetPoint = Point.fromLatLon(
                    message.getFullLocation().getLatitude(), message.getFullLocation().getLongitude());
            double radius = Math.pow(message.getFullLocation().getRadius() + 30, 2);
            while (paths.moveToNext()) {
                PointEntity path = new PointEntity(paths, ctx);
                if (targetPoint.distanceToPathSquared(path.getPoint()) < radius) {
                    return true;
                }
            }
            return false;
        }
    }

    private class MessageDevicePair {
        public String deviceName;
        public String messageId;

        public MessageDevicePair(String deviceName, String messageId) {
            this.deviceName = deviceName;
            this.messageId = messageId;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new String[] {deviceName, messageId});
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MessageDevicePair)) {
                return false;
            }
            MessageDevicePair mdp = (MessageDevicePair) obj;
            return mdp.deviceName.equals(deviceName) && mdp.messageId.equals(messageId);
        }
    }
}
