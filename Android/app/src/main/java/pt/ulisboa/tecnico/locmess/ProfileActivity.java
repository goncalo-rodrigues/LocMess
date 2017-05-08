package pt.ulisboa.tecnico.locmess;

import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
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
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.KeyValueAdapter;
import pt.ulisboa.tecnico.locmess.data.CustomCursorLoader;
import pt.ulisboa.tecnico.locmess.data.entities.Location;
import pt.ulisboa.tecnico.locmess.data.entities.ProfileKeyValue;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;
import pt.ulisboa.tecnico.locmess.serverrequests.RemoveMyFilterTask;
import pt.ulisboa.tecnico.locmess.serverrequests.RequestExistentFiltersTask;
import pt.ulisboa.tecnico.locmess.serverrequests.SetMyFilterTask;

public class ProfileActivity extends ActivityWithDrawer implements KeyValueAdapter.Callback,
        View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>,
        RequestExistentFiltersTask.RequestFiltersTaskCallBack,
        SetMyFilterTask.SetMyFilterTaskCallBack, RemoveMyFilterTask.SetMyFilterTaskCallBack,
        NumberPicker.OnValueChangeListener {

    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private KeyValueAdapter keyValueAdapter;
//    private List<KeyValueAdapter.KeyValue> keyValueList = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private AutoCompleteTextView mKeyAtv;
    private ArrayAdapter mKeyadapter;
    private TextView mNameTv;
    private AutoCompleteTextView mKeyTv;
    private EditText mValueTv;
    private List<String> keys = new ArrayList<>();
    private ImageButton mAddBt;
    private Cursor keyValues;
    ProgressBar waitingBallPb;
    private static Thread before = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        setContentView(R.layout.activity_profile);

//        keyValueList.add(new KeyValueAdapter.KeyValue("keeeey", "valueee"));
//        keyValueList.add(new KeyValueAdapter.KeyValue("keeeey", "valueee"));
//        keyValueList.add(new KeyValueAdapter.KeyValue("keeeey", "valueee"));
        keys.add("keeeey");
        keys.add("keeeeeeey");
        RequestExistentFiltersTask reft = new RequestExistentFiltersTask(this,this);
        reft.execute("");


        keyValueAdapter = new KeyValueAdapter(null, this);


        mRecyclerView = (RecyclerView) findViewById(R.id.profile_key_value_list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(keyValueAdapter);

        mKeyAtv = (AutoCompleteTextView) findViewById(R.id.profile_new_key_autotv);
        mKeyadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, keys);

        mKeyAtv.setAdapter(mKeyadapter);


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

        NetworkGlobalState globalState = (NetworkGlobalState) this.getApplicationContext();
        mNameTv.setText(globalState.getUsername());

        mValueTv = (EditText) findViewById(R.id.profile_new_value_tv);

        mAddBt = ((ImageButton) findViewById(R.id.profile_add_keyval_bt));
        mAddBt.setOnClickListener(this);

        waitingBallPb = (ProgressBar) findViewById(R.id.waiting_ball);

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

        getSupportLoaderManager().initLoader(0, null, this);

        // Number picker for selecting the max number
        NumberPicker np = (NumberPicker) findViewById(R.id.nr_mule);
        np.setMinValue(0);
        np.setMaxValue(20);
        np.setWrapSelectorWheel(true);

        // TODO: Set the right MAX_NUMBER initial value!!!
        np.setValue(10);
        np.setOnValueChangedListener(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRemoveClicked(int position, ProfileKeyValue item) {
        new RemoveMyFilterTask(this, this).execute(item);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_add_keyval_bt:
                // TODO: Validate key / value min and max length
                mAddBt.setVisibility(View.GONE);
                waitingBallPb.setVisibility(View.VISIBLE);
                String key = mKeyAtv.getText().toString();
                String value = mValueTv.getText().toString();
                new SetMyFilterTask(this,this).execute(key, value);


//                keyValueAdapter.notifyItemInserted(keyValueList.size() - 1);;
                break;
            default:
                super.onClick(v);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CustomCursorLoader(this, new CustomCursorLoader.Query() {
            @Override
            public Cursor query(Context context) {
                return ProfileKeyValue.getAll(context);
            }
        });
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        keyValueAdapter.setData(data);
        keyValueAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        keyValueAdapter.setData(null);
        keyValueAdapter.notifyDataSetChanged();
    }


    @Override
    public void OnSearchComplete(ArrayList<String> filters) {
        keys = filters;//todo verify if we are receiving
        mKeyadapter.clear();
        mKeyadapter.addAll(keys);
        mKeyadapter.notifyDataSetChanged();

        Toast.makeText(this, "Received filters"+filters.size(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void SetMyFilterComplete(String key,String value) {
        ProfileKeyValue keyValue = new ProfileKeyValue(key, value);
        keyValue.save(this);
        getSupportLoaderManager().restartLoader(0, null, this);
        mKeyAtv.setText(null);
        mValueTv.setText(null);
        waitingBallPb.setVisibility(View.GONE);
        mAddBt.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSetFilterErrorResponse() {
        //TODO see if someting is needed
        Toast.makeText(this, "Error seting filter", Toast.LENGTH_SHORT).show();
        waitingBallPb.setVisibility(View.GONE);
        mAddBt.setVisibility(View.VISIBLE);
    }

    @Override
    public void RemoveMyFilterComplete(ProfileKeyValue pkv) {
        pkv.delete(this);
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onRemoveErrorResponse() {
        Toast.makeText(this, "Error removing filter", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnNoInternetConnection() {
        Toast.makeText(this, "No internet conection", Toast.LENGTH_SHORT).show();
        waitingBallPb.setVisibility(View.GONE);
        mAddBt.setVisibility(View.VISIBLE);
    }

    /*
     *  Needed for Number Picker
     */

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        if(before != null)
            before.interrupt();

        before = new Thread(new UpdateMaxMuleMsgsThread(newVal));
        before.start();
    }

    private class UpdateMaxMuleMsgsThread implements Runnable {
        private int n;

        public UpdateMaxMuleMsgsThread(int n) {
            this.n = n;
        }

        @Override
        public void run() {
            try {
                // TODO: Update the real MAX_NUMBER, not just print a log message!!!

                Thread.sleep(5000); // Sleeps 5 seconds
                Log.i(LOG_TAG, "The max number would be updated to: " + n);
            } catch (InterruptedException e) {
                // It was killed by a follower thread
                Log.i(LOG_TAG, "A newer size update appeared.");
            }
        }
    }
}
