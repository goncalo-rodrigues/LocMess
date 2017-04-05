package pt.ulisboa.tecnico.locmess.serverrequests;


import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import pt.ulisboa.tecnico.locmess.data.entities.ProfileKeyValue;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 03-04-2017.
 */

public class RemoveMyFilterTask extends AsyncTask<ProfileKeyValue, String, String>{
    private SetMyFilterTaskCallBack callback;
    //private static final String URL_SERVER = "http://requestb.in/16z80wa1";
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;
    ProfileKeyValue savedpkv;


    public RemoveMyFilterTask(SetMyFilterTaskCallBack ltcb, Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }



    @Override
    protected String doInBackground(ProfileKeyValue... params) {
        savedpkv = params[0];
        String key = params[0].getKey();
        String value = params[0].getValue();
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
            URL url = new URL(URL_SERVER+"/remove_filter");
            result= makeHTTPResquest(url,jsoninputs);

            //parse and get json elements ok/nok
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
            callback.onRemoveErrorResponse();

        else if (result.equals("conetionError"))
            callback.OnNoInternetConnection();

        else
            callback.RemoveMyFilterComplete(savedpkv);

        super.onPostExecute(result);
    }


    public interface SetMyFilterTaskCallBack{
        void RemoveMyFilterComplete(ProfileKeyValue pkv);
        void onRemoveErrorResponse();
        void OnNoInternetConnection();
    }

}