package pt.ulisboa.tecnico.locmess.serverrequests;


import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ulisboa.tecnico.locmess.data.entities.ProfileKeyValue;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 26-03-2017.
 */

public class LoginTask extends AsyncTask<String, String,String> {
    private LoginTaskCallBack callback;
    private String result;
    //private static final String URL_SERVER = "http://requestb.in/16z80wa1";
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;
    Context caller;


    public LoginTask(LoginTaskCallBack ltcb,Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
        caller = context;
    }


    @Override
    protected String doInBackground(String... params) {
        String username = params[0];
        String password = params[1];
        String response = "";
        String id = "";
        String key;
        String value;

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        try {
            jsoninputs.put("username", username);
            jsoninputs.put("password", password);

            //open the conection to the server and send
            URL url = new URL(URL_SERVER+"/login");

            response=makeHTTPResquest(url,jsoninputs);

            JSONObject data = new JSONObject(response);
            JSONArray filters = new JSONArray();
            JSONObject filter = null;
            ProfileKeyValue pkv;



            if (data.opt("error") != null) {
                 return  data.getString("error");
            }

            if (data.opt("session_id") == null)
                return "conetionError";

            id=data.getString("session_id");

            if (data.opt("filters") != null) {
                filters = data.getJSONArray("filters");
                for (int j = 0; j < filters.length() - 1; j++) {
                    filter = filters.getJSONObject(j);
                    key = filter.getString("key");
                    value = filter.getString("value");
                    pkv = new ProfileKeyValue(key, value);
                    pkv.save(caller);
                }
            }

            int timestamp;
            if(data.opt("timestamp") != null){
                timestamp = data.getInt("timestamp");
                globalState.setSessionTimestamp(new Date(timestamp));
            }


            globalState.setId(id);
            globalState.setUsername(username);
            return id;
        }catch (JSONException e) {
            e.printStackTrace();
            return "json";
        }catch (IOException e) {
            e.printStackTrace();
            return "conetionError";
        }


        //response = "|"+response+"|";
        //return response;

    }


    @Override
    protected void onPostExecute(String result) {
        if (result.equals("conetionError"))
            callback.OnNoInternetConnection();
        else if(result.equals("wrongCredentials"))
            callback.OnWrongCredentials(result);
        else
        callback.OnLoginComplete(result);
        super.onPostExecute(result);
    }


    public interface LoginTaskCallBack{
        void OnLoginComplete(String id);
        void OnNoInternetConnection();
        void OnWrongCredentials(String error);
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