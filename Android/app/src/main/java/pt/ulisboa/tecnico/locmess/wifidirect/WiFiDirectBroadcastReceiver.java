package pt.ulisboa.tecnico.locmess.wifidirect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by goncalo on 25-03-2017.
 */

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {


    public WiFiDirectBroadcastReceiver() {

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Intent serviceIntent = new Intent(context, WifiDirectService.class);

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                serviceIntent.putExtra(WifiDirectService.EXTRA_COMMAND_KEY, WifiDirectService.COMMAND_START);
                context.startService(serviceIntent);
            } else {
                serviceIntent.putExtra(WifiDirectService.EXTRA_COMMAND_KEY, WifiDirectService.COMMAND_STOP);
                context.startService(serviceIntent);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            serviceIntent.putExtra(WifiDirectService.EXTRA_COMMAND_KEY, WifiDirectService.COMMAND_UPDATE_PEERS);
            context.startService(serviceIntent);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }


}
