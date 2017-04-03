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
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 03-04-2017.
 */

public class CreateLocationTask extends AsyncTask<Void, String,String>{
    private CreateLocationTaskCallBack callback;
    //private static final String URL_SERVER = "http://requestb.in/16z80wa1";
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;
    String errorToReturn = "";
    //Variables to send
    private String name;
    private int lat;
    private int lon;
    private int radius;
    private ArrayList<String> ssids;




    public CreateLocationTask(CreateLocationTaskCallBack ltcb, Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }

    protected String doInBackground(String name, int lat, int lon, int radius ,ArrayList<String> ssids){
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.ssids = ssids;
        return doInBackground();
    }

    @Override
    protected String doInBackground(Void... params) {

        String result ="";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        JSONArray jsonSsids = new JSONArray(ssids);
        JSONObject jsongps = new JSONObject();


        try {
            jsoninputs.put("session_id", globalState.getId());
            jsoninputs.put("name",name);
            jsongps.put("lat",lat);
            jsongps.put("lon",lon);
            jsongps.put("radius",radius);
            jsoninputs.put("gps",jsongps);
            jsoninputs.put("ssids",jsonSsids);

            //open the conection to the server and send
            URL url = new URL(URL_SERVER+"/create_location");
            result= makeHTTPResquest(url,jsoninputs);

            //parse and get json elements, can be an array of locations or a error message

            JSONObject data = new JSONObject(result);
            String resp = data.getString("resp");

            return resp;

        }catch (JSONException e) {e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
            return "conetionError";
        }

        //never reach here unless we get an error parsing the json
        return null;

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


    @Override
    protected void onPostExecute(String result) {
        //TODO see the possible errors and handle them
        if (result.equals("nok"))
            callback.onErrorResponse();

        else if (result.equals("conetionError"))
            callback.OnNoInternetConnection();

        else
            callback.createLocationComplete();

        super.onPostExecute(result);
    }


    public interface CreateLocationTaskCallBack{
        void createLocationComplete();
        void onErrorResponse();
        void OnNoInternetConnection();
    }




}