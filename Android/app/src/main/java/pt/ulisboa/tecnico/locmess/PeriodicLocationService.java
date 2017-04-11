package pt.ulisboa.tecnico.locmess;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
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
import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;
import pt.ulisboa.tecnico.locmess.serverrequests.SendMyLocationTask;
import pt.ulisboa.tecnico.locmess.serverrequests.SendMyLocationTask.SendMyLocationsTaskCallBack;

public class PeriodicLocationService extends Service implements LocationListener, SimWifiP2pManager.PeerListListener,SendMyLocationsTaskCallBack{
    private LocationManager mLocationManager;
    private long minTimeMs = 1000 * 30; // 30 seconds
    private float minDistance = 0;
    private Location mostRecentLocation;
    private List<TimestampedLocation> updates = new ArrayList<>();

    private List<String> ssids = new ArrayList<>();
    private SimWifiP2pBroadcastReceiver mReceiver;

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private final int interval = 1000 * 10; // 1 Second
    private Handler handler = new Handler();

    NetworkGlobalState globalState;
    private static final String URL_SERVER = "http://locmess.duckdns.org";

    private Runnable runnable = new Runnable(){
        public void run() {

            sendToServer();
            handler.postDelayed(runnable, interval);
        }
    };


    @Override
    public void onDestroy() {
        mLocationManager.removeUpdates(this);
        unregisterReceiver(mReceiver);
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Asks for the current location
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, minTimeMs, minDistance, this);
        } catch(SecurityException e) {
            // It should not happen
        }

        Location location = getLastKnownLocation();
        onLocationChanged(location);

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver();
        registerReceiver(mReceiver, filter);

        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        globalState = (NetworkGlobalState) getApplicationContext();

        handler.postDelayed(runnable, interval);


        super.onCreate();
    }

    public PeriodicLocationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onLocationChanged(final Location loc)
    {
        Toast.makeText(this, "New location", Toast.LENGTH_SHORT).show();
        mostRecentLocation = loc;
        updates.add(new TimestampedLocation(ssids, loc.getLatitude(), loc.getLongitude()));

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
        updates.add(new TimestampedLocation(ssids, mostRecentLocation.getLatitude(), mostRecentLocation.getLongitude()));
    }

    @Override
    public void OnSendComplete() {
        Toast.makeText(this, "Send to server complete", Toast.LENGTH_SHORT).show();
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
}
