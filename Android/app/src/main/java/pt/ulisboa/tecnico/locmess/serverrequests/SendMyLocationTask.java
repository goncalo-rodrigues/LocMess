package pt.ulisboa.tecnico.locmess.serverrequests;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import pt.ulisboa.tecnico.locmess.PeriodicLocationService;
import pt.ulisboa.tecnico.locmess.data.entities.FullLocation;
import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;
import pt.ulisboa.tecnico.locmess.PeriodicLocationService.TimestampedLocation;

/**
 * Created by ant on 26-03-2017.
 */

public class SendMyLocationTask extends AsyncTask<Void, String,String>{
    private SendMyLocationsTaskCallBack callback;
    //private static final String URL_SERVER = "http://requestb.in/16z80wa1";
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;
    private ArrayList<Pair> gpsCoordiantes;
    private ArrayList<String> ssids;
    ArrayList<TimestampedLocation> locations;
    Context context;
    ArrayList<String> messagesIDs = new ArrayList<>();
    int numberMessages;

    public SendMyLocationTask(SendMyLocationsTaskCallBack ltcb, Context context, ArrayList<TimestampedLocation> locations){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
        this.locations = locations;
        this.context =context;
    }


    @Override
    protected String doInBackground(Void... params) {
        String result ="";

        try{
            URL url = new URL(URL_SERVER+"/send_locations");
            JSONObject jsoninputs = createJsonMessage(locations);
            result= makeHTTPResquest(url,jsoninputs);

            //parse and get json elements, can be the number of messages or a error message
            JSONObject data = new JSONObject(result);

            if (data.opt("error") != null) {
                String error =  data.getString("error");
                return error;
            }

            numberMessages =data.getInt("n_messages");
            return "ok";

        }catch (JSONException e) {
            e.printStackTrace();
            return "JSONerror";
        }catch (IOException e) {
            e.printStackTrace();
            return "conetionError";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        //TODO see the possible errors and handle them
        if (!result.equals("ok"))
            callback.OnErrorResponse(result);
        else
            callback.OnSendComplete(numberMessages);

        super.onPostExecute(result);
    }

    protected String makeHTTPResquest(URL url,JSONObject jsoninputs) throws IOException {
        HttpURLConnection urlConnection= (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type","application/json");
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);
        urlConnection.connect();

        OutputStreamWriter   out = new   OutputStreamWriter(urlConnection.getOutputStream());
        out.write(jsoninputs.toString());
        out.flush();
        out.close();

        BufferedReader buffer = new BufferedReader( new InputStreamReader(urlConnection.getInputStream(),"utf-8"));
        String result ="";
        String line ;
        while((line=buffer.readLine())!=null) {
            result += line;// +"\n";
        }

        return result;
    }


    private JSONObject createJsonMessage(ArrayList<TimestampedLocation> locations) throws JSONException {
        JSONObject jsoninputs = new JSONObject();
        JSONArray jsonLocations = new JSONArray();

        jsoninputs.put("session_id", globalState.getId());

        JSONArray jsonSsids;
        JSONObject jsonlocation;
        for(TimestampedLocation location : locations){
            jsonlocation = new JSONObject();
            jsonlocation.put("lat",location.latitude);
            jsonlocation.put("long",location.longitude);
            jsonSsids = new JSONArray(location.ssids);
            jsonlocation.put("ssids",jsonSsids);
            jsonlocation.put("timestamp",location.timeStamp.getTime());
            jsonLocations.put(jsonlocation);
        }

        jsoninputs.put("locations",jsonLocations);
        return  jsoninputs;
    }


    public interface SendMyLocationsTaskCallBack{
        void OnSendComplete(int numberMessages);
        void OnErrorResponse(String error);
    }




}