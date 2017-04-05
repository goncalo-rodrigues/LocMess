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
import java.net.URL;
import java.util.ArrayList;

import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 03-04-2017.
 */

public class SetMyFilterTask extends AsyncTask<String, String, String>{
    private SetMyFilterTaskCallBack callback;
    //private static final String URL_SERVER = "http://requestb.in/16z80wa1";
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;
    String key;
    String value;


    public SetMyFilterTask(SetMyFilterTaskCallBack ltcb, Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }


    protected String doInBackground(String key, String value){
        return doInBackground(key,value);
    }


    @Override
    protected String doInBackground(String... params) {
        key = params[0];
        value = params[1];
        String result ="";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        JSONObject jsonfilter = new JSONObject();


        try {
            jsoninputs.put("session_id", globalState.getId());
            jsonfilter.put("key",key);
            jsonfilter.put("value",value);
            jsoninputs.put("filter",jsonfilter);

            //open the conection to the server and send
            URL url = new URL(URL_SERVER+"/set_my_filter");
            result= makeHTTPResquest(url,jsoninputs);

            //parse and get json elements, ok/nok
            JSONObject data = new JSONObject(result);
            String resp = data.getString("resp");

            return resp;

        }catch (JSONException e) {
            e.printStackTrace();
            return "nok";
        }catch (IOException e) {
            e.printStackTrace();
            return "connectionError";
        }

    }


    @Override
    protected void onPostExecute(String result) {
        if (result.equals("nok"))
            callback.onSetFilterErrorResponse();

        else if (result.equals("connectionError"))
            callback.OnNoInternetConnection();

        else
            callback.SetMyFilterComplete(key,value);

        super.onPostExecute(result);
    }


    public interface SetMyFilterTaskCallBack{
        void SetMyFilterComplete(String key,String value);
        void onSetFilterErrorResponse();
        void OnNoInternetConnection();
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


}