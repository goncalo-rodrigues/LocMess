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

import org.json.JSONException;
import org.json.JSONObject;

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


    public LoginTask(LoginTaskCallBack ltcb,Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }


    @Override
    protected String doInBackground(String... params) {
        String username = params[0];
        String password = params[1];
        String response = "";
        int r= 999;
        String id = "";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        try {
            jsoninputs.put("username", username);
            jsoninputs.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();}//TODO make someting

        //open the conection to the server and send
        URL url= null;
        try {
            url = new URL(URL_SERVER+"/login");
            //url = new URL(URL_SERVER);
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
                response+=line;// +"\n";
            }


        } catch (IOException e) {
            e.printStackTrace();
            return "conetionError";
        }

        try {
            JSONObject data = new JSONObject(response);
            id=data.getString("session_id");
            if(id==null||id.length()==0)
                return "conetionError";
            if(id.equals("error")){
                String error = data.getString("error");
                return error;
            }
            globalState.setId(id);
            globalState.setUsername(username);
            return id;
        }catch (JSONException e) {e.printStackTrace(); }


        response = "|"+response+"|";
        return response;

    }

    @Override
    protected void onPostExecute(String result) {
        if (result.equals("conetionError"))
            callback.OnNoInternetConnection(result);
        else if(result.equals("wrongCredentials"))
            callback.OnWrongCredentials(result);
        else
        callback.OnLoginComplete(result);
        super.onPostExecute(result);
    }


    public interface LoginTaskCallBack{
        void OnLoginComplete(String id);
        void OnNoInternetConnection(String error);
        void OnWrongCredentials(String error);
    }
}