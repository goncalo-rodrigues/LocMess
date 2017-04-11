package pt.ulisboa.tecnico.locmess;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.LocationsAdapter;
import pt.ulisboa.tecnico.locmess.serverrequests.RemoveLocationTask;
import pt.ulisboa.tecnico.locmess.serverrequests.RequestLocationsTask;

/**
 * Created by nca on 20-03-2017.
 */

public class LocationsActivity extends ActivityWithDrawer implements LocationsAdapter.Callback, RequestLocationsTask.RequestLocationsTaskCallBack, RemoveLocationTask.RemoveLocationTaskCallBack {
    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private LocationsActivity locAct = this;

    private LocationsAdapter locationsAdapter;
    private List<String> locList = new ArrayList<>();
    private List<String> searchList = new ArrayList<>(locList);
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private RequestLocationsTask task;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_locations);



        locationsAdapter = new LocationsAdapter(searchList, this);

        mRecyclerView = (RecyclerView) findViewById(R.id.locations_list);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(locationsAdapter);

        SearchView sv = ((SearchView) findViewById(R.id.search));
        sv.setIconifiedByDefault(false);

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (task!= null && task.getStatus() != AsyncTask.Status.FINISHED) {
                    task.cancel(false);

                }
                task = new RequestLocationsTask(LocationsActivity.this, LocationsActivity.this);
                task.execute(newText);
                return true;
            }
        });

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        task = new RequestLocationsTask(this, this);
        task.execute();
        super.onResume();
    }

    @Override
    public void onRemoveClicked(int position) {
        RemoveLocationTask removeTask = new RemoveLocationTask(this, this);
        removeTask.execute(locList.get(position));
        locList.remove(position);
        locationsAdapter.notifyItemRemoved(position);
    }

    public void clickNew(View v) {
        Intent intent = new Intent(this, NewLocationActivity.class);
        startActivity(intent);
    }

    @Override
    public void OnLocationSearchComplete(ArrayList<String> locations) {
        locList = locations;
        locationsAdapter.setData(locList);
        locationsAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnErrorResponse(String error) {
        Toast.makeText(this, error == null ? "Error" : error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnNoInternetConnection() {
        Toast.makeText(this, "Unable to retrieve locations from server", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void removeLocationComplete() {

    }

    @Override
    public void onErrorResponse() {

    }

    @Override
    public void onNoInternetConnection() {

    }
}
