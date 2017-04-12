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
import android.widget.Toast;

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
import pt.ulisboa.tecnico.locmess.PeriodicLocationService;
import pt.ulisboa.tecnico.locmess.data.entities.FullLocation;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessage;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessageFilter;
import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;

/**
 * Created by goncalo on 25-03-2017.
 */

public class WifiDirectService extends Service implements SimWifiP2pBroadcastReceiver.Callback, WifiDirectServerWorkerThread.Callback, SimWifiP2pManager.GroupInfoListener, WifiDirectClientThread.Callback {

    private static final String LOG_TAG = WifiDirectService.class.getSimpleName();
    public final static String EXTRA_COMMAND_KEY = "command";
    public final static int COMMAND_START = 1;
    public final static int COMMAND_UPDATE_PEERS = 2;
    public final static int COMMAND_STOP = 3;
    public static final int COMMAND_SEND_MESSAGE = 4;
    private static final int PORT = 10001;

    // wifi direct service
    private Messenger mService;
    private SimWifiP2pManager mManager;
    private SimWifiP2pManager.Channel mChannel;

    // wifi direct thread
    WifiDirectThread wdThread;
    private Collection<SimWifiP2pDevice> devices;
//    private List<String> availableDevices;
    private SimWifiP2pBroadcastReceiver mReceiver;

    // protocol
    private Policy routingPolicy = new Policy();

    //location stuff
    private PeriodicLocationService.PeriodicLocationBinder locBinder;

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

        wdThread = new WifiDirectThread(this, PORT);
        wdThread.start();

        // data structures initialization
//        availableDevices = new ArrayList<>();

        // location stuff
        intent = new Intent(this, PeriodicLocationService.class);
        bindService(intent, anotherConnection, Context.BIND_AUTO_CREATE);
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
                mManager.requestGroupInfo(mChannel, this);
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
        unbindService(anotherConnection);
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
            mManager.requestGroupInfo(mChannel, WifiDirectService.this);
//            mManager.requestPeers(mChannel, WifiDirectService.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
        }
    };

    private ServiceConnection anotherConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            locBinder = (PeriodicLocationService.PeriodicLocationBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locBinder = null;
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
            if (msgToSend.getHops() < 2 && msgToSend.getEndDate().after(new Date()) && msgToSend.getStartDate().before(new Date())) {
                Request request = new Request(Request.REQUEST_MULE_MESSAGE, msgToSend.getJson());
                for (SimWifiP2pDevice device : this.devices) {
                    if (routingPolicy.shouldSendToPeer(device, msgToSend)) {
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
    public void onGroupChanged(SimWifiP2pInfo ginfo) {
        Log.i(LOG_TAG, "group changed");
        mManager.requestGroupInfo(mChannel, this);
    }

    @Override
    public void onPeersChanged() {
        Log.i(LOG_TAG, "peers changed");
//        mManager.requestPeers(mChannel, this);
    }

    @Override
    public Response onNewMessage(Request message) {
        Log.i(LOG_TAG, "received new message " + message.toString());
        switch(message.id) {
            case Request.REQUEST_MULE_MESSAGE:
                MuleMessage m = (MuleMessage) message.getContent();
                m.setHops(m.getHops()+1); // 1 more hop!
                m.save(this);
                if ((getCurrentWifiLocation().isInside(m.getFullLocation()) || getCurrentGPSLocation().isInside(m.getFullLocation()) )
                        && m.amIallowedToReceiveThisMessage(this)) {
                    ReceivedMessage rm = m.toReceived();
                    rm.save(this);
                    Log.i(LOG_TAG,  "Received new decentralized message " + message.toString());
                }
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

//    @Override
//    public void onPeersAvailable(SimWifiP2pDeviceList simWifiP2pDeviceList) {
//        for (SimWifiP2pDevice device : simWifiP2pDeviceList.getDeviceList()){
//            availableDevices.add(device.deviceName);
//        }
//    }

    public FullLocation getCurrentWifiLocation() {
        if (locBinder != null) {
            return locBinder.getLastWifiLocation();
        }
        return new FullLocation("mylocation", new ArrayList<String>());
    }

    public FullLocation getCurrentGPSLocation() {
        if (locBinder != null) {
            return locBinder.getLastGPSLocation();
        }
        // todo: return mock location
        return null;
    }
}
