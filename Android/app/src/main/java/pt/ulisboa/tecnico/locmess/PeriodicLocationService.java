package pt.ulisboa.tecnico.locmess;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.Point;
import pt.ulisboa.tecnico.locmess.data.entities.Message;
import pt.ulisboa.tecnico.locmess.data.entities.FullLocation;
import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;
import pt.ulisboa.tecnico.locmess.data.entities.SSIDSCache;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;
import pt.ulisboa.tecnico.locmess.serverrequests.GetMessagesTask;
import pt.ulisboa.tecnico.locmess.serverrequests.SendMyLocationTask;
import pt.ulisboa.tecnico.locmess.serverrequests.SendMyLocationTask.SendMyLocationsTaskCallBack;

public class PeriodicLocationService extends Service implements LocationListener, SimWifiP2pManager.PeerListListener,SendMyLocationsTaskCallBack, GetMessagesTask.GetMessagesCallBack {
    private LocationManager mLocationManager;
    private long minTimeMs = 0; // 30 seconds
    private float minDistance = 30;
    private Location mostRecentLocation;
    private List<TimestampedLocation> updates = new ArrayList<>();
    private boolean isRequestingLocation = false;

    private List<String> ssids = new ArrayList<>();
    private SimWifiP2pBroadcastReceiver mReceiver;

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private final int interval = 1000 * 10; // 1 Second
    private Handler handler = new Handler();

    NetworkGlobalState globalState;

    private List<Callback> clients = new ArrayList<>();
    private PeriodicLocationBinder mBinder;

    private Runnable runnable = new Runnable(){
        public void run() {

            sendToServer();
            handler.postDelayed(runnable, interval);
        }
    };


    @Override
    public void onDestroy() {
        if (isRequestingLocation)
            mLocationManager.removeUpdates(this);
        unregisterReceiver(mReceiver);
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        requestLocation();

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver();
        registerReceiver(mReceiver, filter);

        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        globalState = (NetworkGlobalState) getApplicationContext();

        handler.postDelayed(runnable, interval);

        mBinder = new PeriodicLocationBinder();

        super.onCreate();
    }

    private void requestLocation() {
        boolean permGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                        == PackageManager.PERMISSION_GRANTED;
        if (permGranted && !isRequestingLocation) {
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, minTimeMs, minDistance, this);
                Location location = getLastKnownLocation();
                onLocationChanged(location);
                isRequestingLocation = true;
            } catch(SecurityException e) {
                // It should not happen, hopefully
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Point temporaryStart = null;
    private Point temporary = null;
    private int i = 0;
    public void onLocationChanged(final Location loc)
    {

        if (loc != null) {
            Toast.makeText(this, "New location", Toast.LENGTH_SHORT).show();
            mostRecentLocation = loc;
//            if (i < 100) {
//                if (temporaryStart == null) {
//                    temporaryStart = Point.fromLatLon(loc.getLatitude(), loc.getLongitude());
//                    temporary = temporaryStart;
//                    temporary.i = i;
//                } else {
//                    temporary.nextPoint = Point.fromLatLon(loc.getLatitude(), loc.getLongitude());
//                    temporary = temporary.nextPoint;
//                    temporary.i=i;
//
//                }
//                i++;
//            }
//            if (i== 100) {
//                Log.d("points before", temporaryStart.toString());
//                temporaryStart.aggregatePoints(0.01);
//                Log.d("points after", temporaryStart.toString());
//                i++;
//            }


            for (Callback client: clients) {
                client.onGPSLocationUpdate(new FullLocation("mylocation", loc.getLatitude(), loc.getLongitude(), 0));
            }
            updates.add(new TimestampedLocation(ssids, loc.getLatitude(), loc.getLongitude()));
        }


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private Location getLastKnownLocation() {
        if (mLocationManager == null) return null;
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            try {
                Location l = mLocationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            } catch (SecurityException e) {
                // wont happen
            }

        }
        return bestLocation;
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        ssids.clear();
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            ssids.add(device.deviceName);
        }
        for (Callback client: clients) {
            client.onWifiLocationUpdate(new FullLocation("mylocation", new ArrayList<>(ssids)));
            SSIDSCache.insertOrUpdate(ssids, this);
        }
        if (mostRecentLocation != null)
            updates.add(new TimestampedLocation(ssids, mostRecentLocation.getLatitude(), mostRecentLocation.getLongitude()));
        else
            updates.add(new TimestampedLocation(ssids, 0, 0));

    }

    @Override
    public void OnSendComplete(int numberMessages) {
        //TODO Gonçalo the messages are here delete the next and do what you want
        /*for(String mid: messagesIDs)
            m.save(this);*/

        if (numberMessages > 0) {
            NotificationsHelper.startNewMessageNotification(this);
        }
        Toast.makeText(this, "Location Send: "+numberMessages+" available", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnErrorResponse(String error) {
        Toast.makeText(this, "Error:"+error, Toast.LENGTH_SHORT).show();
    }

    public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            String action = intent.getAction();
            if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Toast.makeText(context, "Peers changed",
                        Toast.LENGTH_SHORT).show();
                mManager.requestPeers(mChannel, PeriodicLocationService.this);

            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mManager = new SimWifiP2pManager(new Messenger(service));
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
            mManager.requestPeers(mChannel, PeriodicLocationService.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mManager = null;
            mChannel = null;
        }
    };

    private void sendToServer(){
        if(updates.size()==0){
            Toast.makeText(this, "No location to send", Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO: add locations to build common paths
        ArrayList<TimestampedLocation> copy = new ArrayList<>(updates);
        updates = new ArrayList<>();
        new SendMyLocationTask(this,this,copy).execute();

    }



    public class TimestampedLocation {
        public Date timeStamp;
        public List<String> ssids;
        public double latitude;
        public double longitude;

        public TimestampedLocation(List<String> ssids, double latitude, double longitude) {
            this.ssids = ssids;
            this.latitude = latitude;
            this.longitude = longitude;
            this.timeStamp = new Date();
        }
    }



    public class PeriodicLocationBinder extends Binder {
        public FullLocation getLastGPSLocation() {

            if (mostRecentLocation != null) {
                return new FullLocation("mylocation", mostRecentLocation.getLatitude(), mostRecentLocation.getLongitude(), 0);
            } else {
                // try to get last known location
                if ((mostRecentLocation = getLastKnownLocation()) != null) {
                    return new FullLocation("mylocation", mostRecentLocation.getLatitude(), mostRecentLocation.getLongitude(), 0);
                }
                return null;
            }
        }

        public FullLocation getLastWifiLocation() {
            return new FullLocation("mylocation", new ArrayList<>(ssids));
        }

        public void registerClient(Callback callback) {
            requestLocation();
            clients.add(callback);
        }

        public void unregisterClient(Callback callback) {
            clients.remove(callback);
        }

        public void reevaluatePermission() {
            requestLocation();
        }

        public void getMessages() {
            ArrayList<TimestampedLocation> locations = new ArrayList<>();
            locations.add(new TimestampedLocation(ssids, mostRecentLocation.getLatitude(), mostRecentLocation.getLongitude()));
            GetMessagesTask task = new GetMessagesTask(PeriodicLocationService.this, PeriodicLocationService.this, locations);
            task.execute();
        }


    }

    @Override
    public void OnGetMessagesComplete(ArrayList<ReceivedMessage> messages) {
        for (ReceivedMessage m: messages) {
            m.save(PeriodicLocationService.this);
        }
        for (Callback client: clients) {
            client.onNewMessagesInDb();
        }
    }

    @Override
    public void OnGetMessagesError(String error) {

    }
    public interface Callback {
        void onGPSLocationUpdate(FullLocation location);
        void onWifiLocationUpdate(FullLocation location);
        void onNewMessagesInDb();
    }
}
