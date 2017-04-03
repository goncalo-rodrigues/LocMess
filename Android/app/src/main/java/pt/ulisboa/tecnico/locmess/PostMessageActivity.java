package pt.ulisboa.tecnico.locmess;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import pt.ulisboa.tecnico.locmess.data.entities.CreatedMessage;
import pt.ulisboa.tecnico.locmess.data.entities.Message;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.FilterAdapter;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

import static java.lang.Math.random;

public class PostMessageActivity extends ActivityWithDrawer implements FilterAdapter.Callback, View.OnClickListener,
        TimePicker.TimePickerCallback, DatePicker.DatePickerCallback{

    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private ArrayList<String> locationList = new ArrayList<>();
    private ArrayAdapter<String> locationListAdapter;

    private FilterAdapter filtersAdapter;
    private List<FilterAdapter.KeyValue> filterList = new ArrayList<>();
    private List<String> filterKeys = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private AutoCompleteTextView mLocationAtv;

    private EditText messageTextET;

    private RadioButton mCentralizededRadio;
    private RadioButton mAdOcRadio;

    private AutoCompleteTextView mKeyAtv;
    private TextView mValueTv;
    private RadioButton mblacklistedRadio;
    private RadioButton mWhitelistedRadio;

    private String messageToSend;

    private Button startTimeButton;
    private Button startDateButton;
    private Button endTimeButton;
    private Button endDateButton;

    private Calendar startDate;
    private Calendar endDate;
    private boolean start;

    NetworkGlobalState globalState;







    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_post_message);

        //Locations
        getLocations();
        mLocationAtv = (AutoCompleteTextView) findViewById(R.id.send_m_location);
        locationListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationList);
        mLocationAtv.setAdapter(locationListAdapter);
        // show dropdown when focused
        mLocationAtv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mLocationAtv.showDropDown();
                }
            }
        });

        messageTextET = (EditText) findViewById(R.id.messageText);

        //Date and time selector
        initTimes();

        startTimeButton = (Button) findViewById(R.id.start_time_button);
        startDateButton = (Button) findViewById(R.id.start_date_button);
        endTimeButton   = (Button) findViewById(R.id.end_time_button);
        endDateButton   = (Button) findViewById(R.id.end_date_button);

        String stringDate = DateFormat.getTimeInstance().format(startDate.getTime());
        startTimeButton.setText(stringDate);
        startTimeButton.setOnClickListener(this);

        stringDate = DateFormat.getDateInstance().format(startDate.getTime());
        startDateButton.setText(stringDate);
        startDateButton.setOnClickListener(this);

        stringDate = DateFormat.getTimeInstance().format(endDate.getTime());
        endTimeButton.setText(stringDate);
        endTimeButton.setOnClickListener(this);

        stringDate = DateFormat.getDateInstance().format(endDate.getTime());
        endDateButton.setText(stringDate);
        endDateButton.setOnClickListener(this);

        //Distribution
        mCentralizededRadio = (RadioButton) findViewById(R.id.radio_button_Centralized);
        mCentralizededRadio.setChecked(true);
        mAdOcRadio = (RadioButton) findViewById(R.id.radio_button_adOc);


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

        mblacklistedRadio =(RadioButton) findViewById(R.id.radio_button_black);
        mWhitelistedRadio = (RadioButton) findViewById(R.id.radio_button_white);
        mWhitelistedRadio.setChecked(true);

        globalState = (NetworkGlobalState) getApplicationContext();

        super.onCreate(savedInstanceState);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_m_add_keyval_bt:
                // TODO: Validate key / value min and max length
                filterList.add(new FilterAdapter.KeyValue(
                        mKeyAtv.getText().toString(), mValueTv.getText().toString(),mblacklistedRadio.isChecked()));
                mKeyAtv.setText(null);
                mValueTv.setText(null);

                filtersAdapter.notifyItemInserted(filterList.size() - 1);;
                break;
        //TODO a partir daqui nao tao a funcionar
            case R.id.start_time_button:
                showStartTimePickerDialog(v);
                break;

            case R.id.start_date_button:
                showStartDatePickerDialog(v);
                break;

            case R.id.end_time_button:
                showEndTimePickerDialog(v);
                break;

            case R.id.end_date_button:
                showEndDatePickerDialog(v);
                break;

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
        start =true;
        TimePicker newFragment = new TimePicker();
        newFragment.setDate(startDate);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }



    public void showEndTimePickerDialog(View v) {
        start =false;
        TimePicker newFragment = new TimePicker();
        newFragment.setDate(endDate);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }


    public void showStartDatePickerDialog(View v) {
        start =true;
        DatePicker newFragment = new DatePicker();
        newFragment.setDate(startDate);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }


    public void showEndDatePickerDialog(View v) {
        start =false;
        DatePicker newFragment = new DatePicker();
        newFragment.setDate(endDate);
        newFragment.show(getSupportFragmentManager(), "datePicker");
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


    @Override// this method is used from Select Time to make a callback
    public void onSetTime(int hourOfDay, int minute) {
        if(start){
            startDate.set(Calendar.HOUR_OF_DAY,hourOfDay);
            startDate.set(Calendar.MINUTE,minute);
            String stringDate = DateFormat.getTimeInstance().format(startDate.getTime());
            startTimeButton.setText(stringDate);
            return;
        }
        endDate.set(Calendar.HOUR_OF_DAY,hourOfDay);
        endDate.set(Calendar.MINUTE,minute);
        String stringDate = DateFormat.getTimeInstance().format(endDate.getTime());
        endTimeButton.setText(stringDate);

    }

    @Override// this method is used from Select Date to make a callback
    public void onSetDate(int year, int month, int dayOfMonth) {
        if(start){
            startDate.set(year,month,dayOfMonth);
            String stringDate = DateFormat.getDateInstance().format(startDate.getTime());
            startDateButton.setText(stringDate);
            return;
        }
        endDate.set(year,month,dayOfMonth);
        String stringDate = DateFormat.getDateInstance().format(endDate.getTime());
        endDateButton.setText(stringDate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.send_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_send was selected
            case R.id.action_send:
                Toast.makeText(this, "Pressed message send message!",Toast.LENGTH_LONG).show();
                //TODO request id to the server and get author from global variables
                String id = String.valueOf((int) (random()*221313161));
                //id = send request to server

                if(mAdOcRadio.isActivated()){
                    //AD-OC mode

                }

                else{
                    //Centralized mod is activated
                }


                CreatedMessage message = new CreatedMessage(id,messageTextET.getText().toString(), globalState.getUsername(), mLocationAtv.getText().toString(), startDate.getTime(), endDate.getTime());
                message.save(this);
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
