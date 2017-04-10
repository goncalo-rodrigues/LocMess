package pt.ulisboa.tecnico.locmess.wifidirect;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessage;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessageFilter;

/**
 * Created by goncalo on 25-03-2017.
 */

public class WifiDirectService extends Service implements WifiP2pManager.PeerListListener, SimWifiP2pBroadcastReceiver.Callback, WifiDirectServerWorkerThread.Callback, SimWifiP2pManager.GroupInfoListener, WifiDirectClientThread.Callback {

    private static final String LOG_TAG = WifiDirectService.class.getSimpleName();
    public final static String EXTRA_COMMAND_KEY = "command";
    public final static int COMMAND_START = 1;
    public final static int COMMAND_UPDATE_PEERS = 2;
    public final static int COMMAND_STOP = 3;
    private static final int COMMAND_SEND_MESSAGE = 4;
    private static final int PORT = 10001;

    // wifi direct service
    private Messenger mService;
    private SimWifiP2pManager mManager;
    private SimWifiP2pManager.Channel mChannel;
    private boolean mBound = false;

    // wifi direct thread
    WifiDirectThread wdThread;
    private Collection<SimWifiP2pDevice> devices;
    private SimWifiP2pBroadcastReceiver mReceiver;

    // protocol
    private Policy routingPolicy = new Policy();

    @Override
    public void onCreate() {
        super.onCreate();
        SimWifiP2pSocketManager.Init(getApplicationContext());

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(mReceiver, filter);

        // start wifi direct
        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;

        wdThread = new WifiDirectThread(this, PORT);
        wdThread.start();

//        serverThread = new WifiDirectThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        int reqCode = intent.getIntExtra(EXTRA_COMMAND_KEY, 1);
        if (!wdThread.isAlive()) {
            wdThread.start();
        }
        switch (reqCode) {
            case COMMAND_START:
                // do nothing
                break;
            case COMMAND_SEND_MESSAGE:
                // to do
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
        wdThread.exit();
        unregisterReceiver(mReceiver);
        unbindService(mConnection);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
            mBound = true;
            mManager.requestGroupInfo(mChannel, WifiDirectService.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
            mBound = false;
        }
    };



    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

        // compile list of network members
        StringBuilder peersStr = new StringBuilder();
        this.devices = new ArrayList<>();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null)?"??":device.getVirtIp()) + ")\n";
            peersStr.append(devstr);
            this.devices.add(device);
        }

        Log.i(LOG_TAG, peersStr.toString());
        Cursor m = MuleMessage.getAll(this);
        m.moveToFirst();
        while (!m.isAfterLast()) {
            MuleMessage msgToSend = new MuleMessage(m, this);
            if (msgToSend.getHops() < 2) {
                MuleMessage message =  new MuleMessage(m, this);
                Request request = new Request(Request.REQUEST_MULE_MESSAGE, message.getJson());
                for (SimWifiP2pDevice device : this.devices) {
                    if (routingPolicy.shouldSendToPeer(device, message)) {
                        WifiDirectClientThread t = new WifiDirectClientThread(device.getVirtIp(), PORT, request, this);
                        t.start();
                    }
                }
            }
            m.moveToNext();
        }
//        MuleMessage m = new MuleMessage("id123", "content 123", "author 123", "location 123", new Date(), new Date(), new ArrayList<MuleMessageFilter>(), 0);
//        m.save(this);
//        sendToAll(new Request(0, m.getJson()));
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {

    }

    @Override
    public void onGroupChanged(SimWifiP2pInfo ginfo) {
        mManager.requestGroupInfo(mChannel, this);
    }

    @Override
    public Response onNewMessage(Request message) {
        switch(message.id) {
            case Request.REQUEST_MULE_MESSAGE:
                MuleMessage m = (MuleMessage) message.getContent();
                m.setHops(m.getHops()+1); // 1 more hop!
                m.save(this);


                return new Response(true);
            default:
                return new Response(false); // protocol unknown
        }
    }
    @Override
    public Request onNewResponse(Response response) {
        Log.i(LOG_TAG, "received response " + response.success);
        return null;
    }
}
