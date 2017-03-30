package pt.ulisboa.tecnico.locmess.serverrequests;


import android.content.Context;
import android.os.AsyncTask;

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

import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 26-03-2017.
 */

public class RequestLocationsTask extends AsyncTask<String, String,ArrayList<String>>{
    private LoginTaskCallBack callback;
    //private static final String URL_SERVER = "http://requestb.in/16z80wa1";
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;


    public RequestLocationsTask(LoginTaskCallBack ltcb, Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }


    @Override
    protected ArrayList<String> doInBackground(String... params) {
        String startswith = params[0];
        String result ="";
        ArrayList<String> response = new ArrayList<String>();
        String id = "";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        try {
            jsoninputs.put("session_id", globalState.getId());
            jsoninputs.put("startswith", startswith);
        } catch (JSONException e) {
            e.printStackTrace();}//TODO make someting

        //open the conection to the server and send
        URL url= null;
        try {
            url = new URL(URL_SERVER+"/request_locations");
        } catch (MalformedURLException e) { e.printStackTrace(); }

        try{
            HttpURLConnection urlConnection=null;
            urlConnection = (HttpURLConnection) url.openConnection();
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

            String line ="";
            while((line=buffer.readLine())!=null){
                result+=line;// +"\n";
            }


        } catch (IOException e) {
            e.printStackTrace();
            response.add("conetionError");
            return response;
        }

        try {
            JSONObject data = new JSONObject(result);
            String error = data.getString("error");

            if(error!=null && error.length()>0){
                response.add(error);
                response.add(error);
                return response;
            }
            JSONArray locations =data.getJSONArray("locations");
            for (int j=0;j<locations.length()-1;j++)
                response.add(locations.getString(j));

            return response;
        }catch (JSONException e) {e.printStackTrace(); }


        return response;

    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        //TODO see the possible errors and handle them
        if (result.get(0).equals("error")){
            callback.OnErrorResponse(result.get(1));
        }
        callback.OnSearchComplete(result);
        super.onPostExecute(result);
    }


    public interface LoginTaskCallBack{
        void OnSearchComplete(ArrayList<String> locations);
        void OnErrorResponse(String error);
    }
}