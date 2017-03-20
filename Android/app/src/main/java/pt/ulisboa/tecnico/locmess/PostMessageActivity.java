package pt.ulisboa.tecnico.locmess;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Dictionary;
import java.util.Date;

import pt.ulisboa.tecnico.locmess.adapters.KeyValueAdapter;

public class PostMessageActivity extends ActivityWithDrawer implements KeyValueAdapter.Callback, View.OnClickListener {

    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private ArrayList<String> locationList = new ArrayList<>();
    private ArrayAdapter<String> locationListAdapter;

    private KeyValueAdapter filtersKeyValueAdapter;
    private List<KeyValueAdapter.KeyValue> filterList = new ArrayList<>();
    private List<String> filterKeys = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private AutoCompleteTextView mKeyAtv;
    private TextView mValueTv;

    private String messageToSend;

    private Button startTimeButton;
    private Button startDateButton;
    private Button endTimeButton;
    private Button endDateButton;

    Date startDate;
    Date endDate;






    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_post_message);

        //Locations
        getLocations();
        Spinner spinner = (Spinner) findViewById(R.id.locationsSpinner);
        locationListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, locationList);
        locationListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(locationListAdapter);

        //Date and time selector
        initTimes();

        startTimeButton = (Button) findViewById(R.id.start_time_button);
        startDateButton = (Button) findViewById(R.id.start_date_button);
        endTimeButton   = (Button) findViewById(R.id.end_time_button);
        endDateButton   = (Button) findViewById(R.id.end_date_button);

        String stringDate = DateFormat.getTimeInstance().format(startDate);
        startTimeButton.setText(stringDate);

        stringDate = DateFormat.getDateInstance().format(startDate);
        startDateButton.setText(stringDate);

        stringDate = DateFormat.getTimeInstance().format(endDate);
        endTimeButton.setText(stringDate);

        stringDate = DateFormat.getDateInstance().format(endDate);
        endDateButton.setText(stringDate);








        //Filters
        getFiltersList();

        mRecyclerView = (RecyclerView) findViewById(R.id.send_message_key_value_list);
        filtersKeyValueAdapter = new KeyValueAdapter(filterList, this);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(filtersKeyValueAdapter);

        mKeyAtv = (AutoCompleteTextView) findViewById(R.id.send_m_new_key_autotv);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filterKeys);

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

        mValueTv = (TextView) findViewById(R.id.send_m_new_value_tv);

        ((ImageButton) findViewById(R.id.send_m_add_keyval_bt)).setOnClickListener(this);



        super.onCreate(savedInstanceState);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_m_add_keyval_bt:
                // TODO: Validate key / value min and max length
                filterList.add(new KeyValueAdapter.KeyValue(
                        mKeyAtv.getText().toString(), mValueTv.getText().toString()));
                mKeyAtv.setText(null);
                mValueTv.setText(null);

                filtersKeyValueAdapter.notifyItemInserted(filterList.size() - 1);;
                break;
            default:
                Log.w(LOG_TAG, "On click event not yet implemented for this view.");
        }
    }


    private void getLocations(){
        //TODO this should load locations from database
        locationList.add("location1");
        locationList.add("location2");
        locationList.add("location3");
        locationList.add("location4");
    }

    private void getFiltersList(){
        //TODO this should get possible keys for filters
        filterKeys.add("country");
        filterKeys.add("Sex");
        filterKeys.add("Color");
    }


    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePicker();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePicker();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void setDate(){}

    public void setTime(){}

    @Override
    public void onRemoveClicked(int position) {
        filterList.remove(position);
        filtersKeyValueAdapter.notifyItemRemoved(position);
    }

    private void initTimes(){
        startDate= new Date();
        long timePlus1hour = startDate.getTime() + 3600000;
        endDate = new Date(timePlus1hour);

    }
}
