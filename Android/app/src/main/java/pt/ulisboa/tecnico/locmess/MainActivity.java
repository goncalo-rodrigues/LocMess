package pt.ulisboa.tecnico.locmess;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.RunnableFuture;

import pt.ulisboa.tecnico.locmess.adapters.MessagesAdapter;
import pt.ulisboa.tecnico.locmess.adapters.Pager;
import pt.ulisboa.tecnico.locmess.data.entities.CreatedMessage;
import pt.ulisboa.tecnico.locmess.data.entities.FullLocation;
import pt.ulisboa.tecnico.locmess.data.entities.Message;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessage;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessageFilter;
import pt.ulisboa.tecnico.locmess.data.entities.PointEntity;
import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;
import pt.ulisboa.tecnico.locmess.data.entities.SSIDSCache;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;
import pt.ulisboa.tecnico.locmess.wifidirect.WifiDirectService;

public class MainActivity extends ActivityWithDrawer implements BaseMessageFragment.Callback, TabLayout.OnTabSelectedListener, PeriodicLocationService.Callback {

    private static final int REQUEST_LOCATION = 1;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Pager adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PeriodicLocationService.PeriodicLocationBinder locBinder;
    private boolean mBound = false;
    private AlarmManager alarmMgr;

    @Override
    protected void onDestroy() {
        if (mBound) {
            unbindService(mConnection);
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        if (((NetworkGlobalState) getApplicationContext()).getId() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }


        tabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        viewPager = (ViewPager) findViewById(R.id.main_view_pager);

        adapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount(), this);

        //Adding adapter to pager
        viewPager.setAdapter(adapter);


        //Adding onTabSelectedListener to swipe views
        tabLayout.addOnTabSelectedListener(this);

        // Onclick for FAB
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fabMain);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage(v);
            }
        });

        Intent myIntent = new Intent(this, WifiDirectService.class);
        startService(myIntent);

        checkGPSStatus();

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.get("notification") != null) {
            bindService(new Intent(this, PeriodicLocationService.class), mConnection, Context.BIND_AUTO_CREATE);
        }

        deleteStuffFromDB.start();

        PendingIntent alarmIntent = PendingIntent.getBroadcast( this, 0, new Intent(getPackageName() + ".ALARM"), 0 );
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY,
                AlarmManager.INTERVAL_DAY, alarmIntent);

        super.onCreate(savedInstanceState);
    }

    protected void sendMessage(View v){
        Intent intent = new Intent(this, PostMessageActivity.class);
        startActivity(intent);
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
        } else {
            Intent myIntent = new Intent(this, PeriodicLocationService.class);
            startService(myIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkGPSStatus();
                } else {
                    Toast.makeText(this, "Please accept gps permission", Toast.LENGTH_LONG).show();
                    Intent myIntent = new Intent(this, PeriodicLocationService.class);
                    startService(myIntent);
                }
            }
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }
    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
    @Override
    public void onRemove(Message message) {

    }


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBound = true;
            locBinder = (PeriodicLocationService.PeriodicLocationBinder) service;
            locBinder.getMessages();
            locBinder.registerClient(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locBinder = null;
            mBound = false;
        }
    };

    @Override
    public void onGPSLocationUpdate(FullLocation location) {

    }

    @Override
    public void onWifiLocationUpdate(FullLocation location) {

    }

    @Override
    public void onNewMessagesInDb() {
        adapter.getNewMessagesTab().restartLoader();
    }


    private Thread deleteStuffFromDB = new Thread(new Runnable() {

        @Override
        public void run() {
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -7);
            Date beforeDate = cal.getTime();
            SSIDSCache.removeAllBefore(beforeDate, MainActivity.this);
            PointEntity.removeAllBefore(beforeDate, MainActivity.this);
        }
    });
}
