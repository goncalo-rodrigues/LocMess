package pt.ulisboa.tecnico.locmess;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import pt.ulisboa.tecnico.locmess.adapters.DrawerListAdapter;
import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;
import pt.ulisboa.tecnico.locmess.serverrequests.LogoutTask;

import static android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION;

/**
 * Created by root on 14-03-2017.
 */

public abstract class ActivityWithDrawer extends AppCompatActivity implements DrawerListAdapter.Callback, View.OnClickListener,
        LogoutTask.LogoutCallBack {


    private static final String LOG_TAG = ActivityWithDrawer.class.getSimpleName();
    private String[] mDrawerTitles;
    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mDrawerPosition;
    private ArrayList<DrawerListAdapter.DrawerItem> mDrawerItems = new ArrayList<>();
    private LinearLayout mDrawer;
    private LinearLayout mLogoutBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        // get views
        mDrawerLayout = (DrawerLayout) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        mDrawer = (LinearLayout)  mDrawerLayout.findViewById(R.id.left_drawer);
        mDrawerList = (RecyclerView) mDrawerLayout.findViewById(R.id.left_drawer_list);
        mLogoutBt = (LinearLayout) mDrawerLayout.findViewById(R.id.left_drawer_logout_ll);

        // fill variables
        mDrawerPosition = getDrawerPosition();

        mDrawerTitles = getResources().getStringArray(R.array.drawer_items);

        mDrawerItems.add(new DrawerListAdapter.DrawerItem(mDrawerTitles[0], R.drawable.ic_message_black));
        mDrawerItems.add(new DrawerListAdapter.DrawerItem(mDrawerTitles[1], R.drawable.ic_place_black));
        mDrawerItems.add(new DrawerListAdapter.DrawerItem(mDrawerTitles[2], R.drawable.ic_person_black));


        // setup recycler view

        mDrawerList.setLayoutManager(new LinearLayoutManager(this));
        // Set the adapter for the list view
        final DrawerListAdapter adapter = new DrawerListAdapter(mDrawerItems, mDrawerPosition, this);
        mDrawerList.setAdapter(adapter);


        // add drawer toggle

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mLogoutBt.setOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }
    // Called when invalidateOptionsMenu() is invoked
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
////        menu.findItem(R.id.action_search).setVisible(!drawerOpen);
//        return super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_drawer_logout_ll:
                new LogoutTask(this,this).execute();
                break;
            default:
                Log.w(LOG_TAG, "On click event not yet implemented for this view.");

        }
    }


    @Override
    public void logoutComplete(){
        Toast.makeText(this, "logout Completed", Toast.LENGTH_LONG).show();
        logoutClear();
    }

    @Override
    public void logoutErrorResponse(){
        Toast.makeText(this, "logout Error Response", Toast.LENGTH_LONG).show();
        logoutClear();
    }

    @Override
    public void OnNoInternetConnection(){
        Toast.makeText(this, "logout No Internet Connection", Toast.LENGTH_LONG).show();
        logoutClear();
    }

    private void logoutClear(){
        clearDatabase();
        Intent intent;
        intent = new Intent(this, InitActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }

    private void clearDatabase(){
        LocmessDbHelper helper = new LocmessDbHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(LocmessContract.SQL_DELETE_1);
        db.execSQL(LocmessContract.SQL_DELETE_2);
        db.execSQL(LocmessContract.SQL_DELETE_3);
        db.execSQL(LocmessContract.SQL_DELETE_5);
        db.execSQL(LocmessContract.SQL_DELETE_6);

        db.execSQL(LocmessContract.SQL_CREATE_1);
        db.execSQL(LocmessContract.SQL_CREATE_2);
        db.execSQL(LocmessContract.SQL_CREATE_4);
        db.execSQL(LocmessContract.SQL_CREATE_5);
        db.execSQL(LocmessContract.SQL_CREATE_6);

    }

    /** Starts new activity **/
    @Override
    public void onItemClick(int position) {
        Intent intent;
        switch(position) {
            case 0:
                intent = new Intent(this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            case 1:
                // start Locations
                intent = new Intent(this, LocationsActivity.class);
                intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            case 2:
                // start ProfileActivity
                intent = new Intent(this, ProfileActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                break;
        }

        // Highlight the selected item, update the title, and close the drawer
//        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawer);
    }

    private int getDrawerPosition() {
        if (this instanceof MainActivity) {
            return 0;
        }
        if (this instanceof LocationsActivity) {
            return 1;
        }
        if (this instanceof ProfileActivity) {
            return 2;
        }
        return -1;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
