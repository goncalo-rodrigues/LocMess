package pt.ulisboa.tecnico.locmess;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static pt.ulisboa.tecnico.locmess.R.string.location;

/**
 * Created by nca on 20-03-2017.
 */

public class NewLocationActivity extends ActivityWithDrawer implements LocationListener {
    private static final int REQUEST_LOCATION = 0;
    private static final int REQUEST_WIFI_SCAN = 1;
    private Context context = this;

    private double latitude = 100;
    private double longitude = 200;
    private LocationManager mLocationManager;

    private WifiManager wifi = null;
    private WifiReceiver recv = null;
    private List<ScanResult> scanRes;
    private List<String> ssids = null;
    private ArrayAdapter<String> arrayAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_new_location);
        super.onCreate(savedInstanceState);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        recv = new WifiReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(recv);
    }

    public void gpsClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        if(!checked)
            return;

        LinearLayout l = (LinearLayout) findViewById(R.id.chose_gps);
        l.setVisibility(View.VISIBLE);

        l = (LinearLayout) findViewById(R.id.chose_wifi);
        l.setVisibility(View.GONE);
    }

    public void wifiClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        if(!checked)
            return;

        LinearLayout l = (LinearLayout) findViewById(R.id.chose_gps);
        l.setVisibility(View.GONE);

        l = (LinearLayout) findViewById(R.id.chose_wifi);
        l.setVisibility(View.VISIBLE);

        // Makes sure that WiFi is enabled
        if (!wifi.isWifiEnabled()) {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);
        }

        boolean permGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
                        == PackageManager.PERMISSION_GRANTED;

        // Asks for permission
        if(!permGranted)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                    REQUEST_WIFI_SCAN);

        // Receives WiFi scan results
        registerReceiver(recv, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        checkGPSStatus();
        wifi.startScan();
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
                    REQUEST_WIFI_SCAN);
        }

        // Checks if the GPS is enabled
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
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

            case REQUEST_WIFI_SCAN: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Receives WiFi scan results
                    registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context c, Intent intent) {
                            scanRes = wifi.getScanResults();
                        }
                    }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Toast t = Toast.makeText(this, "Status", Toast.LENGTH_SHORT);
        t.show();
    }

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

    // Used for discovering the wifi networks
    private class WifiReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context c, Intent intent) {
            scanRes = wifi.getScanResults();

            if(ssids == null && scanRes.size() > 0) {
                ssids = new ArrayList<>();

                for (ScanResult sc : scanRes)
                    ssids.add(sc.SSID);

                Collections.sort(ssids);
                ListView lv = (ListView) findViewById(R.id.wifi_ids_list);
                arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, ssids);
                lv.setAdapter(arrayAdapter);
            }
        }
    }
}
