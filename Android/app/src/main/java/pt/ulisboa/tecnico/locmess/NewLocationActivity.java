package pt.ulisboa.tecnico.locmess;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Created by nca on 20-03-2017.
 */

public class NewLocationActivity extends ActivityWithDrawer implements LocationListener {
    private static final int REQUEST_LOCATION = 0;
    LocationManager mLocationManager;
    double latitude = 100;
    double longitude = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_new_location);
        super.onCreate(savedInstanceState);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        boolean permGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        if(permGranted) {
            Toast t = Toast.makeText(this, "Permission was granted", Toast.LENGTH_SHORT);
            t.show();
        }

        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
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
    }

    public void getCurrentLocation(View v) {
        Toast t;

        if(!(longitude == 200 && latitude == 100)) {
            EditText et = (EditText) findViewById(R.id.latitude);
            et.setText(String.valueOf(latitude));
            et = (EditText) findViewById(R.id.longitude);
            et.setText(String.valueOf(longitude));

            t = Toast.makeText(this, "Updated coordinates", Toast.LENGTH_SHORT);
            t.show();
        }

        t = Toast.makeText(this, "Use my location", Toast.LENGTH_SHORT);
        t.show();
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

                    try {
                        location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    } catch(SecurityException e) {
                        // It should not happen because permission was granted
                        e.printStackTrace();
                    }

                    if(location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        Toast t = Toast.makeText(this, "Started with new permission", Toast.LENGTH_SHORT);
                        t.show();
                    }

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
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }
}
