package pt.ulisboa.tecnico.locmess;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
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
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.ulisboa.tecnico.locmess.data.entities.FullLocation;
import pt.ulisboa.tecnico.locmess.serverrequests.CreateLocationTask;
import pt.ulisboa.tecnico.locmess.wifidirect.SimWifiP2pBroadcastReceiver;

/**
 * Created by nca on 20-03-2017.
 */

public class NewLocationActivity extends ActivityWithDrawer implements LocationListener, SimWifiP2pManager.PeerListListener, View.OnClickListener, CreateLocationTask.CreateLocationTaskCallBack {
    private static final int REQUEST_LOCATION = 0;
    private static final int REQUEST_WIFI_SCAN = 1;
    private Context context = this;

    private double latitude = 100;
    private double longitude = 200;
    private LocationManager mLocationManager;

    private List<ScanResult> scanRes;
    private List<String> ssids = null;
    private ArrayAdapter<String> arrayAdapter = null;
    private boolean finishedWifiSearch = false;
    private SimWifiP2pBroadcastReceiver mReceiver;

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;

    private Button mCreateButton;
    private EditText mNameEditText;
    private RadioGroup mRadioGroup;
    private EditText mLatitudeEditText;
    private EditText mLongitudeEditText;
    private EditText mRadiusEditText;
    private TextView mSsidsEmptyTv;
    private ListView mSsidsLv;
    private ProgressDialog mSendingProcessDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_new_location);
        super.onCreate(savedInstanceState);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        ssids = new ArrayList<>();
        mSsidsLv = (ListView) findViewById(R.id.wifi_ids_list);
        arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, ssids);
        mSsidsLv.setAdapter(arrayAdapter);

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver();
        registerReceiver(mReceiver, filter);

        Intent intent = new Intent(this, SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mCreateButton = (Button) findViewById(R.id.newlocation_create_bt);
        mNameEditText = (EditText) findViewById(R.id.location_name);
        mRadioGroup = (RadioGroup) findViewById(R.id.newlocation_radiogroup);
        mLatitudeEditText = (EditText) findViewById(R.id.latitude);
        mLongitudeEditText = (EditText) findViewById(R.id.longitude);
        mRadiusEditText = (EditText) findViewById(R.id.radius);
        mSsidsEmptyTv = (TextView) findViewById(R.id.newlocation_wifis_empty_tv);
        mSendingProcessDialog = new ProgressDialog(this);
        mSendingProcessDialog.setMessage(getString(R.string.sending_message));
        mCreateButton.setOnClickListener(this);
        checkIfSsidsEmpty();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mConnection);
    }

    public void gpsClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        if(!checked)
            return;

        LinearLayout l = (LinearLayout) findViewById(R.id.chose_gps);
        l.setVisibility(View.VISIBLE);

        FrameLayout f = (FrameLayout) findViewById(R.id.chose_wifi);
        f.setVisibility(View.GONE);
    }

    public void wifiClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        if(!checked)
            return;

        LinearLayout l = (LinearLayout) findViewById(R.id.chose_gps);
        l.setVisibility(View.GONE);

        FrameLayout f = (FrameLayout) findViewById(R.id.chose_wifi);
        f.setVisibility(View.VISIBLE);

        checkGPSStatus();
        checkWifiStatus();

    }

    public void getCurrentLocation(View v) {
        checkGPSStatus();

        // Asks for the current location
        try {
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch(SecurityException e) {
            // It should not happen
        }

        Location location = getLastKnownLocation();
        onLocationChanged(location);
    }

    private void checkGPSStatus() {
        boolean permGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        // Asks for permission
        if(!permGranted) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }

        // Checks if the GPS is enabled
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void checkWifiStatus() {
        // Makes sure that WiFi is enabled

        boolean permGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                        == PackageManager.PERMISSION_GRANTED;

        // Asks for permission
        if(!permGranted)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                    REQUEST_WIFI_SCAN);
    }

    public void getMapLocation(View v) {
        //TODO

        String uri = "geo:0,0?q=ist"; //TODO: Remove the hardcoded string!!!
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    /*
    Methods used for getting the gps location
     */

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Location location = null;

                    location = getLastKnownLocation();
                    onLocationChanged(location);

                    try {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                    } catch(SecurityException e) {
                        // It should not happen because permission was granted
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast t = Toast.makeText(this, "Location changed", Toast.LENGTH_SHORT);
        t.show();

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            EditText et = (EditText) findViewById(R.id.latitude);
            et.setText(String.valueOf(latitude));
            et = (EditText) findViewById(R.id.longitude);
            et.setText(String.valueOf(longitude));
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }


    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
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
        arrayAdapter.notifyDataSetChanged();
        checkIfSsidsEmpty();
    }

    @Override
    public void createLocationComplete() {
        mSendingProcessDialog.cancel();
        finish();
    }

    @Override
    public void onErrorResponse() {
        mSendingProcessDialog.cancel();
        Toast.makeText(this, R.string.error_server, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnNoInternetConnection() {
        mSendingProcessDialog.cancel();
        Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
    }

    private void checkIfSsidsEmpty() {
        if (ssids.size() == 0) {
            mSsidsEmptyTv.setVisibility(View.VISIBLE);
            mSsidsLv.setVisibility(View.GONE);
        } else {
            mSsidsEmptyTv.setVisibility(View.GONE);
            mSsidsLv.setVisibility(View.VISIBLE);
        }

    }


    public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            String action = intent.getAction();
            if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                Toast.makeText(context, "Peers changed",
                        Toast.LENGTH_SHORT).show();
                mManager.requestPeers(mChannel, NewLocationActivity.this);

            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mManager = new SimWifiP2pManager(new Messenger(service));
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
            mManager.requestPeers(mChannel, NewLocationActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mManager = null;
            mChannel = null;
        }
    };

    private double getLatitude() {
        return Double.parseDouble(mLatitudeEditText.getText().toString());
    }

    private double getLongitude() {
        return Double.parseDouble(mLongitudeEditText.getText().toString());
    }

    private double getRadius() {
        return Double.parseDouble(mRadiusEditText.getText().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newlocation_create_bt:
                CreateLocationTask task = new CreateLocationTask(this, this);
                FullLocation loc;
                String name = mNameEditText.getText().toString();
                if (mRadioGroup.getCheckedRadioButtonId() == R.id.gps) {
                    try {
                        loc = new FullLocation(name, getLatitude(), getLongitude(), getRadius());
                        task.execute(loc);
                        mSendingProcessDialog.show();
                    } catch (NumberFormatException | NullPointerException e) {
                        Toast.makeText(this, R.string.invalid_fields_gps, Toast.LENGTH_LONG).show();
                    }


                } else if (mRadioGroup.getCheckedRadioButtonId() == R.id.wifi) {
                    if (ssids.size() == 0) {
                        Toast.makeText(this, R.string.no_nearby_wifis, Toast.LENGTH_LONG).show();
                    } else {
                        loc = new FullLocation(name, ssids);
                        task.execute(loc);
                        mSendingProcessDialog.show();
                    }
                } else {
                    Toast.makeText(this, R.string.select_one_gps_wifi, Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onClick(v);
        }

    }
}
