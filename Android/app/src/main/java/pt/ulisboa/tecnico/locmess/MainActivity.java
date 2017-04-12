package pt.ulisboa.tecnico.locmess;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ListView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.MessagesAdapter;
import pt.ulisboa.tecnico.locmess.adapters.Pager;
import pt.ulisboa.tecnico.locmess.data.entities.CreatedMessage;
import pt.ulisboa.tecnico.locmess.data.entities.Message;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessage;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessageFilter;
import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;
import pt.ulisboa.tecnico.locmess.wifidirect.WifiDirectService;

public class MainActivity extends ActivityWithDrawer implements BaseMessageFragment.Callback, TabLayout.OnTabSelectedListener{

    private static final int REQUEST_LOCATION = 1;
    private Pager adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private IBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

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



        super.onCreate(savedInstanceState);
    }

    protected void sendMessage(View v){
//        ReceivedMessage m = new ReceivedMessage("1", "text", "author", "loc", new Date(), new Date());
//        m.save(this);
//        CreatedMessage m2 = new CreatedMessage("2", "text", "author", "loc", new Date(), new Date());
//        m2.save(this);
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
                    // TODO
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
}
