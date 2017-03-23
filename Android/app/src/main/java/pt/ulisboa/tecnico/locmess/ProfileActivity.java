package pt.ulisboa.tecnico.locmess;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.KeyValueAdapter;

public class ProfileActivity extends ActivityWithDrawer implements KeyValueAdapter.Callback, View.OnClickListener {

    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private KeyValueAdapter keyValueAdapter;
    private List<KeyValueAdapter.KeyValue> keyValueList = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private AutoCompleteTextView mKeyAtv;
    private TextView mNameTv;
    private AutoCompleteTextView mKeyTv;
    private EditText mValueTv;
    private List<String> keys = new ArrayList<>();
    private ImageButton mAddBt;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        setContentView(R.layout.activity_profile);

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

        mKeyAtv = (AutoCompleteTextView) findViewById(R.id.profile_new_key_autotv);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, keys);

        mKeyAtv.setAdapter(adapter);


        // show dropdown when focused
        mKeyAtv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mKeyAtv.showDropDown();
                }
            }
        });
        mNameTv = (TextView) findViewById(R.id.profile_name_tv);

        mNameTv.setText("Goncalo");

        mValueTv = (EditText) findViewById(R.id.profile_new_value_tv);

        mAddBt = ((ImageButton) findViewById(R.id.profile_add_keyval_bt));
        mAddBt.setOnClickListener(this);

        mValueTv.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onClick(mAddBt);
                    handled = true;
                }
                return handled;
            }
        });

        super.onCreate(savedInstanceState);

    }

    @Override
    public void onRemoveClicked(int position) {
        keyValueList.remove(position);
        keyValueAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_add_keyval_bt:
                // TODO: Validate key / value min and max length
                keyValueList.add(new KeyValueAdapter.KeyValue(
                        mKeyAtv.getText().toString(), mValueTv.getText().toString()));
                mKeyAtv.setText(null);
                mValueTv.setText(null);

                keyValueAdapter.notifyItemInserted(keyValueList.size() - 1);;
                break;
            default:
                super.onClick(v);
        }
    }
}
