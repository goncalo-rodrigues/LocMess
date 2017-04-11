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
 * Created by ant on 30-03-2017.
 */

public class RequestExistentFiltersTask extends AsyncTask<String, String,ArrayList<String>>{
    private RequestFiltersTaskCallBack callback;
    //private static final String URL_SERVER = "http://requestb.in/16z80wa1";
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;


    public RequestExistentFiltersTask(RequestFiltersTaskCallBack ltcb, Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }


    @Override
    protected ArrayList<String> doInBackground(String... params) {
        String startswith = params[0];
        String result ="";
        ArrayList<String> response = new ArrayList<>();

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        try {
            jsoninputs.put("session_id", globalState.getId());
            jsoninputs.put("startswith", startswith);

            //open the conection to the server and send
            URL url = new URL(URL_SERVER+"/get_keys");

            result=makeHTTPResquest(url,jsoninputs);

            //parse and get json elements, can be an array of filters or a error message
            JSONObject data = new JSONObject(result);


            if (data.opt("error") != null) {
                response.add("error");
                response.add(data.getString("error"));
                return  response;
            }
            if (data.opt("keys") != null) {
                JSONArray filters = data.getJSONArray("keys");
                for (int j = 0; j < filters.length() ; j++)
                    response.add(filters.getString(j));
            }
            return response;

        }catch (JSONException e) {e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
            response.add("conetionError");
            return response;
        }

        return response;

    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        //TODO see the possible errors and handle them
        String first= "";
        if (result.size()>0)
            first = result.get(0);

        if (first.equals("error")||first.equals("conetionError"))
            callback.OnNoInternetConnection();

        else
            callback.OnSearchComplete(result);

        super.onPostExecute(result);
    }


    public interface RequestFiltersTaskCallBack{
        void OnSearchComplete(ArrayList<String> filters);
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