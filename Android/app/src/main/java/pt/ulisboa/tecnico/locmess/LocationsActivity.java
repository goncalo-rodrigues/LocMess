package pt.ulisboa.tecnico.locmess;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.LocationsAdapter;

/**
 * Created by nca on 20-03-2017.
 */

public class LocationsActivity extends ActivityWithDrawer implements LocationsAdapter.Callback {
    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private LocationsActivity locAct = this;

    private LocationsAdapter locationsAdapter;
    private List<LocationsAdapter.LocValue> locList = new ArrayList<>();
    private List<LocationsAdapter.LocValue> searchList = new ArrayList<>(locList);
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_locations);

        // TODO: Start removing when done
        addLoc(new LocationsAdapter.LocValue("eduroam-rnl"));
        addLoc(new LocationsAdapter.LocValue("arco do cego"));
        addLoc(new LocationsAdapter.LocValue("h3-saldanha"));
        // TODO: End of removal

        locationsAdapter = new LocationsAdapter(searchList, this);

        mRecyclerView = (RecyclerView) findViewById(R.id.locations_list);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(locationsAdapter);

        ((SearchView) findViewById(R.id.search)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.equals("")) {
                    searchList = new ArrayList<>();
                    for(LocationsAdapter.LocValue l : locList) {
                        if(l.loc.contains(newText))
                            searchList.add(l);
                    }
                }

                else
                    searchList = locList;

                locationsAdapter = new LocationsAdapter(searchList, locAct);
                mRecyclerView.setAdapter(locationsAdapter);

                return true;
            }
        });

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRemoveClicked(int position) {
        updateStructures(position);
        locationsAdapter.notifyItemRemoved(position);
    }

    private void updateStructures(int position) {
        LocationsAdapter.LocValue val = searchList.get(position);

        searchList.remove(val);
        locList.remove(val);
    }

    public void clickNew(View v) {
        Intent intent = new Intent(this, NewLocationActivity.class);
        startActivity(intent);
    }

    public void addLoc(LocationsAdapter.LocValue val) {
        searchList.add(val);
        locList.add(val);
    }
}
