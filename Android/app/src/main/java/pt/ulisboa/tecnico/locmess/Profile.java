package pt.ulisboa.tecnico.locmess;

import android.inputmethodservice.Keyboard;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.KeyValueAdapter;
import pt.ulisboa.tecnico.locmess.adapters.MessagesAdapter;

public class Profile extends ActivityWithDrawer implements KeyValueAdapter.Callback {

    private KeyValueAdapter keyValueAdapter;
    private List<KeyValueAdapter.KeyValue> keyValueList = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private AutoCompleteTextView mKeyTextView;
    private TextView mNameTv;
    private List<String> keys = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        setContentView(R.layout.activity_profile);

        keyValueList.add(new KeyValueAdapter.KeyValue("keeeey", "valueee"));
        keyValueList.add(new KeyValueAdapter.KeyValue("keeeey", "valueee"));
        keyValueList.add(new KeyValueAdapter.KeyValue("keeeey", "valueee"));
        keyValueList.add(new KeyValueAdapter.KeyValue("keeeey", "valueee"));
        keyValueList.add(new KeyValueAdapter.KeyValue("keeeey", "valueee"));
        keyValueList.add(new KeyValueAdapter.KeyValue("keeeey", "valueee"));
        keyValueList.add(new KeyValueAdapter.KeyValue("keeeey", "valueee"));
        keyValueList.add(new KeyValueAdapter.KeyValue("keeeey", "valueee"));
        keys.add("keeeey");
        keys.add("keeeeeeey");

        keyValueAdapter = new KeyValueAdapter(keyValueList, this);


        mRecyclerView = (RecyclerView) findViewById(R.id.profile_key_value_list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(keyValueAdapter);

        mKeyTextView = (AutoCompleteTextView) findViewById(R.id.profile_new_key_autotv);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, keys);

        mKeyTextView.setAdapter(adapter);

        mNameTv = (TextView) findViewById(R.id.profile_name_tv);

        super.onCreate(savedInstanceState);

    }

    @Override
    public void onRemoveClicked(int position) {
        keyValueList.remove(position);
        keyValueAdapter.notifyDataSetChanged();
    }
}
