package pt.ulisboa.tecnico.locmess.wifidirect;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by goncalo on 25-03-2017.
 */

public class WifiDirectService extends Service implements WifiP2pManager.PeerListListener {

    private static final String LOG_TAG = WifiDirectService.class.getSimpleName();
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiDirectThread serverThread;
    private List<WifiP2pDevice> connectedDevices;
    private Collection<WifiP2pDevice> availableDevices;
    public final static String EXTRA_COMMAND_KEY = "command";
    public final static int COMMAND_START = 1;
    public final static int COMMAND_UPDATE_PEERS = 2;
    public final static int COMMAND_STOP = 3;

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        connectedDevices = new ArrayList<>();
        availableDevices = new ArrayList<>();
        serverThread = new WifiDirectThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int reqCode = intent.getIntExtra(EXTRA_COMMAND_KEY, 1);
        if (!serverThread.isAlive()) {
            serverThread.start();
        }
        switch (reqCode) {
            case COMMAND_START:
                discoverPeers();
                break;
            case COMMAND_UPDATE_PEERS:
                mManager.requestPeers(mChannel, this);
                break;
            case COMMAND_STOP:
                // stop worker thread
                stopSelf();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        serverThread.exit();
        super.onDestroy();
    }

    private void connectToPeer(final WifiP2pDevice peer) {
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = peer.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                connectedDevices.add(peer);
                Log.i(LOG_TAG, "Connected to " + peer.deviceName);
            }

            @Override
            public void onFailure(int reason) {

            }
        });

    }

    private void discoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // just wait for broadcast receiver
            }

            @Override
            public void onFailure(int reasonCode) {
                // at least we tried
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        if (peers != null) {
            availableDevices = peers.getDeviceList();
            for (WifiP2pDevice device : availableDevices) {
                connectToPeer(device);
            }
        }
    }
}
