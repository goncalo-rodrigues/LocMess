package pt.ulisboa.tecnico.locmess;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import pt.ulisboa.tecnico.locmess.data.Point;
import pt.ulisboa.tecnico.locmess.data.entities.PointEntity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> paths;
    private Button aggregateBt;
    private Button removeMarketsBt;
    private Button deleteAllBt;
    private Button refreshBt;
    private int selected = 0;
    private boolean drawMarkers = true;
    private PointEntity currentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final ListView lv = (ListView) findViewById(R.id.maps_path_lv);
        removeMarketsBt = (Button) findViewById(R.id.maps_remove_markers);
        aggregateBt = (Button) findViewById(R.id.maps_aggregate);
        refreshBt = (Button) findViewById(R.id.maps_refresh);
        deleteAllBt = (Button) findViewById(R.id.maps_delete_all_paths);
        paths = new ArrayList<>();

        getPaths();

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, paths);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mMap != null) {
                    drawPath(mMap, position);
                }
                selected = position;
            }
        });

        aggregateBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = PointEntity.getAllPaths(MapsActivity.this);
                cursor.moveToPosition(selected);
                PointEntity p = new PointEntity(cursor, MapsActivity.this);
                p.getPoint().aggregatePoints(10);
                p.savePath(MapsActivity.this);
                cursor.close();
                drawPath(mMap, selected);
            }
        });

        refreshBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPaths();
                mAdapter.notifyDataSetChanged();
                if (selected >= 0 && paths.size() > selected)
                    drawPath(mMap, selected);
                else
                    selected = -1;
            }
        });

        deleteAllBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PointEntity.removeAllBefore(new Date(), MapsActivity.this);
                mMap.clear();
                selected = -1;
                paths.clear();
                mAdapter.notifyDataSetChanged();
            }
        });

        removeMarketsBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawMarkers = !drawMarkers;
                drawPath(mMap, selected);
            }
        });



    }

    private void getPaths() {
        paths.clear();
        Cursor c = PointEntity.getAllPaths(this);
        int count = c.getCount();
        c.close();
        for (int i=0; i<count; i++) paths.add("path" + i);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        drawPath(googleMap, 0);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Point target = Point.fromLatLon(latLng.latitude, latLng.longitude);
                double distance = Math.sqrt(target.distanceToPathSquared(currentPath.getPoint()));
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Distance to path " + distance)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        });
    }

    public void drawPath(GoogleMap map, int which) {

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.clear();
        PolylineOptions rectOptions = new PolylineOptions();
        //this is the color of route
        rectOptions.color(Color.argb(255, 85, 166, 27));
        map.setMyLocationEnabled(true);
        Cursor c = PointEntity.getAllPaths(MapsActivity.this);
        if (c.getCount() <= which) return;
        c.moveToPosition(which);
        currentPath = new PointEntity(c, MapsActivity.this);
        Point currentPoint = currentPath.getPoint();
        LatLng latlng = null;
        while (currentPoint != null) {
            double lati = currentPoint.getLat();
            double longi = currentPoint.getLon();

            latlng = new LatLng(lati,
                    longi);

            if (drawMarkers) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latlng);


                markerOptions.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED));

                map.addMarker(markerOptions);
            }



            rectOptions.add(latlng);


            map.addPolyline(rectOptions);
            currentPoint = currentPoint.nextPoint;
        }

        map.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        map.animateCamera(CameraUpdateFactory.zoomTo(Math.max(13, map.getCameraPosition().zoom)));

        c.close();


    }
}
