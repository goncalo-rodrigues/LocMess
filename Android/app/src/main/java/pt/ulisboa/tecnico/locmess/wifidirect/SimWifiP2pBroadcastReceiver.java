package pt.ulisboa.tecnico.locmess.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.widget.Toast;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.ulisboa.tecnico.locmess.MainActivity;

public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

    private Callback mCallback;
    private MainActivity mContext;

    public SimWifiP2pBroadcastReceiver(Callback callback, MainActivity activity) {
        super();
        this.mContext = activity;
        this.mCallback = callback;
    }
    public SimWifiP2pBroadcastReceiver(Callback callback) {
        super();
        this.mCallback = callback;
    }
    @Override
    public void onReceive(android.content.Context context, Intent intent) {
        String action = intent.getAction();
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

        	// This action is triggered when the Termite service changes state:
        	// - creating the service generates the WIFI_P2P_STATE_ENABLED event
        	// - destroying the service generates the WIFI_P2P_STATE_DISABLED event

            int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);

            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
        		Toast.makeText(context, "WiFi Direct enabled",
        				Toast.LENGTH_SHORT).show();
            } else {
        		Toast.makeText(context, "WiFi Direct disabled",
        				Toast.LENGTH_SHORT).show();
            }

        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()

        	Toast.makeText(context, "Peer list changed",
    				Toast.LENGTH_SHORT).show();
            if (mCallback != null)
                mCallback.onPeersChanged();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

        	SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
        			SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
        	ginfo.print();
            if (mCallback != null)
                mCallback.onGroupChanged(ginfo);
    		Toast.makeText(context, "Network membership changed",
    				Toast.LENGTH_SHORT).show();

        } else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {

        	SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
        			SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
        	ginfo.print();
    		Toast.makeText(context, "Group ownership changed",
    				Toast.LENGTH_SHORT).show();
        }
    }

    public interface Callback {
        void onGroupChanged(SimWifiP2pInfo ginfo);
        void onPeersChanged();
    }
}
