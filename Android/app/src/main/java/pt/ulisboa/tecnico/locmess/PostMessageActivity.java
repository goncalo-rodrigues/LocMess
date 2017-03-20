package pt.ulisboa.tecnico.locmess;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.FilterAdapter;

public class PostMessageActivity extends ActivityWithDrawer implements FilterAdapter.Callback, View.OnClickListener {

    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private ArrayList<String> locationList = new ArrayList<>();
    private ArrayAdapter<String> locationListAdapter;

    private FilterAdapter filtersAdapter;
    private List<FilterAdapter.KeyValue> filterList = new ArrayList<>();
    private List<String> filterKeys = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private AutoCompleteTextView mKeyAtv;
    private TextView mValueTv;
    private ToggleButton mblacklistedTb;

    private String messageToSend;

    private Button startTimeButton;
    private Button startDateButton;
    private Button endTimeButton;
    private Button endDateButton;

    Calendar startDate;
    Calendar endDate;






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

        String stringDate = DateFormat.getTimeInstance().format(startDate.getTime());
        startTimeButton.setText(stringDate);

        stringDate = DateFormat.getDateInstance().format(startDate.getTime());
        startDateButton.setText(stringDate);

        stringDate = DateFormat.getTimeInstance().format(endDate.getTime());
        endTimeButton.setText(stringDate);

        stringDate = DateFormat.getDateInstance().format(endDate.getTime());
        endDateButton.setText(stringDate);


        //Filters
        getFiltersList();

        mRecyclerView = (RecyclerView) findViewById(R.id.send_message_key_value_list);
        filtersAdapter = new FilterAdapter(filterList, this);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(filtersAdapter);

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

        mblacklistedTb = (ToggleButton) findViewById(R.id.toggle_Button_Blacklist);

        super.onCreate(savedInstanceState);
    }



    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_m_add_keyval_bt:
                // TODO: Validate key / value min and max length
                filterList.add(new FilterAdapter.KeyValue(
                        mKeyAtv.getText().toString(), mValueTv.getText().toString(),mblacklistedTb.isChecked()));
                mKeyAtv.setText(null);
                mValueTv.setText(null);

                filtersAdapter.notifyItemInserted(filterList.size() - 1);;
                break;
        //TODO a partir daqui nao tao a funcionar
            case R.id.start_time_button:
                showStartTimePickerDialog(v);

            case R.id.start_date_button:
                showStartDatePickerDialog(v);

            case R.id.end_time_button:
                showEndTimePickerDialog(v);

            case R.id.end_date_button:
                showEndDatePickerDialog(v);

            default:
                Log.w(LOG_TAG, "On click event not yet implemented for this view.");
        }
    }


    private void getLocations(){
        //TODO this should load locations from database
        locationList.add("IST");
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


    public void showStartTimePickerDialog(View v) {
        TimePicker newFragment = new TimePicker();
        newFragment.setDate(startDate);
        newFragment.show(getSupportFragmentManager(), "timePicker");
        startDate = newFragment.getDate();
        String stringDate = DateFormat.getTimeInstance().format(startDate.getTime());
        startTimeButton.setText(stringDate);
    }

    public void showEndTimePickerDialog(View v) {
        TimePicker newFragment = new TimePicker();
        newFragment.setDate(endDate);
        newFragment.show(getSupportFragmentManager(), "timePicker");

        endDate = newFragment.getDate();
        String stringDate = DateFormat.getTimeInstance().format(endDate.getTime());
        endTimeButton.setText(stringDate);
    }


    public void showStartDatePickerDialog(View v) {
        DatePicker newFragment = new DatePicker();
        newFragment.setDate(startDate);
        newFragment.show(getSupportFragmentManager(), "datePicker");
        startDate = newFragment.getDate();
        String stringDate = DateFormat.getDateInstance().format(startDate.getTime());
        startDateButton.setText(stringDate);
    }

    public void showEndDatePickerDialog(View v) {
        DatePicker newFragment = new DatePicker();
        newFragment.setDate(endDate);
        newFragment.show(getSupportFragmentManager(), "datePicker");
        endDate = newFragment.getDate();
        String stringDate = DateFormat.getDateInstance().format(endDate.getTime());
        endDateButton.setText(stringDate);
    }


    @Override
    public void onRemoveClicked(int position) {
        filterList.remove(position);
        filtersAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onChangeBlacklist(int Position) {
        //TODO i dont know yet what to do
    }

    private void initTimes(){
        startDate= Calendar.getInstance();
        endDate = Calendar.getInstance();
        endDate.add(Calendar.HOUR_OF_DAY,1);

    }
}
