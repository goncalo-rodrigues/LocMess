package pt.ulisboa.tecnico.locmess;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by nca on 20-03-2017.
 */

public class LocationsActivity extends ActivityWithDrawer {
    private ArrayAdapter arrayAdapter;
    private ListView mListView;
    private List<String> locs = new ArrayList<>();
    private List<String> search_locs = locs;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_locations);

        // TODO: Start removing when done
        locs.add("eduroam-rnl");
        locs.add("arco do cego");
        locs.add("h3-saldanha");

        Collections.sort(search_locs);
        // TODO: End of removal

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, search_locs);

        mListView = (ListView) findViewById(R.id.locations_list);
        mListView.setAdapter(arrayAdapter);

        ((SearchView) findViewById(R.id.search)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.equals("")) {
                    search_locs = new ArrayList<>();
                    for(String s : locs) {
                        if(s.contains(newText))
                            search_locs.add(s);
                    }
                }

                else
                    search_locs = locs;

                arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, search_locs);
                mListView.setAdapter(arrayAdapter);

                return true;
            }
        });

        super.onCreate(savedInstanceState);
    }

    // TODO: Add the delete button for each location
}
