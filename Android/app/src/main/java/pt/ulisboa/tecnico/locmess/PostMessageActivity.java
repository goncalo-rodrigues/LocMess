package pt.ulisboa.tecnico.locmess;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import pt.ulisboa.tecnico.locmess.data.entities.CreatedMessage;
import pt.ulisboa.tecnico.locmess.data.entities.FullLocation;
import pt.ulisboa.tecnico.locmess.data.entities.Message;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.adapters.FilterAdapter;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessage;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessageFilter;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;
import pt.ulisboa.tecnico.locmess.serverrequests.GetLocationInfoTask;
import pt.ulisboa.tecnico.locmess.serverrequests.PostMessageTask;
import pt.ulisboa.tecnico.locmess.serverrequests.RequestExistentFiltersTask;
import pt.ulisboa.tecnico.locmess.serverrequests.RequestLocationsTask;
import pt.ulisboa.tecnico.locmess.wifidirect.WifiDirectService;

import static java.lang.Math.random;

public class PostMessageActivity extends ActivityWithDrawer implements FilterAdapter.Callback, View.OnClickListener,
        TimePicker.TimePickerCallback, DatePicker.DatePickerCallback, PostMessageTask.PostMessageTaskCallBack, RequestExistentFiltersTask.RequestFiltersTaskCallBack, RequestLocationsTask.RequestLocationsTaskCallBack, GetLocationInfoTask.GetLocationInfoCallBack {

    private ScrollView fullScreen;
    private ProgressBar progressBarSending;

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
    ArrayAdapter keysListAdapter;
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
    private boolean waiting = false;

    NetworkGlobalState globalState;

    private CreatedMessage message; // variable used to save message while answer from server
                                    // dont come

    //elements to save
    String id;
    String messageText ;
    String username ;
    String location ;
    Date startD ;
    Date endD ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_post_message);

        fullScreen =(ScrollView) findViewById(R.id.ScrolView1);
        progressBarSending = (ProgressBar) findViewById(R.id.progressBarSending);
        progressBarSending.setVisibility(View.GONE);

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
        keysListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filterKeys);

        mKeyAtv.setAdapter(keysListAdapter);
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

    //Get Existent locations from the server
    private void getLocations(){
        RequestLocationsTask rlt = new RequestLocationsTask(this,this);
        rlt.execute("");
    }

    //Get the existent filters keys from the server
    private void getFiltersList(){
        RequestExistentFiltersTask reft = new RequestExistentFiltersTask(this,this);
        reft.execute("");
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
                if(waiting == false) {
                    setScreenWaiting();

                    //Store the state that at the moment of send existed
                    messageText = messageTextET.getText().toString();
                    username = globalState.getUsername();
                    location = mLocationAtv.getText().toString();
                    startD = startDate.getTime();
                    endD = endDate.getTime();
                    id = Utils.buildMessageId(this, !mAdOcRadio.isChecked());

                    if (mAdOcRadio.isChecked())
                        postMessageAdOc();

                    else
                        postMessageCentralized();
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void postMessageCentralized(){
        ArrayList<Pair> whitelisted = new ArrayList<>();
        ArrayList<Pair> blacklisted = new ArrayList<>();
        ArrayList<MuleMessageFilter> filters = new ArrayList<>();
        Pair filter;
        for(FilterAdapter.KeyValue kv : filterList){
            filter = new Pair(kv.key, kv.value);
            if(kv.blacklisted)
                blacklisted.add(filter);
            else
                whitelisted.add(filter);
        }

        message = new CreatedMessage(id,messageText, username, location, startDate.getTime(), endDate.getTime(), true);

        new PostMessageTask(this,this,username,location,startD,endD,messageText,
                whitelisted,blacklisted,id).execute();

    }

    private void postMessageAdOc(){
        //All the work id done when the callback from Async task in made
        //until then nothing can be done
        new GetLocationInfoTask(this,this,location).execute();
    }

//----------------------    Set the state of the screen     ----------------------------//

    private void setScreenWaiting(){
        waiting = true;
        fullScreen.setVisibility(View.GONE);
        progressBarSending.setVisibility(View.VISIBLE);
    }



    private void setScreenNormal(){
        waiting = false;
        progressBarSending.setVisibility(View.GONE);
        fullScreen.setVisibility(View.VISIBLE);
    }

//----------------------------------------------------------------------------------------
//----         Callbacks from the requests made to the server by Assink tasks         ----
//----------------------------------------------------------------------------------------
    @Override
    public void PostMessageComplete() {
        message.save(this);
        Toast.makeText(this, "Completed post message", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onErrorResponse() {
        Toast.makeText(this, "No error in request response", Toast.LENGTH_SHORT).show();
        setScreenNormal();
    }

    @Override
    public void OnGetLocationInfoComplete(FullLocation flocation) {
        ArrayList<MuleMessageFilter> filters = new ArrayList<>();
        MuleMessageFilter mmf;
        for(FilterAdapter.KeyValue kv : filterList){
            mmf = new MuleMessageFilter(id, kv.key, kv.value, kv.blacklisted);
            filters.add(mmf);
        }

        MuleMessage muleM = new MuleMessage(id, messageText, username, flocation, startD,
                endD,filters, 0);
        muleM.save(this);

        CreatedMessage messageAdOc = new CreatedMessage(id,messageText, username, location, startDate.getTime(), endDate.getTime(), false);
        messageAdOc.save(this);
        Toast.makeText(this, "Ad-oc message", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, WifiDirectService.class);
        intent.putExtra(WifiDirectService.EXTRA_COMMAND_KEY, WifiDirectService.COMMAND_SEND_MESSAGE);
        startService(intent);
        finish();
    }

    @Override
    public void OnGetInfoErrorResponse(String error) {
        Toast.makeText(this, "ERROR:"+error, Toast.LENGTH_SHORT).show();
        setScreenNormal();
    }

    @Override
    public void OnNoInternetConnection() {
        Toast.makeText(this, "No Internet connection", Toast.LENGTH_SHORT).show();
        setScreenNormal();
    }

    @Override
    public void OnSearchComplete(ArrayList<String> filters) {
        filterKeys = filters;//todo verify if we are receiving
        keysListAdapter.clear();
        keysListAdapter.addAll(filterKeys);
        keysListAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Received filters"+filters, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnLocationSearchComplete(ArrayList<String> locations) {
        locationList = locations;
        locationListAdapter.clear();
        locationListAdapter.addAll(locations);
        locationListAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Received Locations"+locations.size(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void OnErrorResponse(String error) {
        Toast.makeText(this, "Error:"+error, Toast.LENGTH_SHORT).show();
        setScreenNormal();
    }
}
