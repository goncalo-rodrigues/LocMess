package pt.ulisboa.tecnico.locmess.serverrequests;


import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ant on 26-03-2017.
 */

public class LoginTask extends AsyncTask<String, String,String> {
    private LoginTaskCallBack callback;
    private String result;
    private static final String URL_SERVER = "http://requestb.in/16z80wa1";


    public LoginTask(LoginTaskCallBack ltcb){
        callback = ltcb;
    }

    @Override
    protected String doInBackground(String... params) {
        String username = params[0];
        String password = params[1];
        String response = "";
        int r= 999;

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
            url = new URL(URL_SERVER);
        } catch (MalformedURLException e) { e.printStackTrace(); }

        try{
            HttpURLConnection urlConnection=null;
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type","application/json");
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


            JSONObject data = new JSONObject(response);


        } catch (IOException e) { e.printStackTrace();
        } catch (JSONException e) {e.printStackTrace(); }


        response = "|"+response+"|";
        return response;

    }

    @Override
    protected void onPostExecute(String result) {
        callback.OnLoginComplete(result);
        super.onPostExecute(result);
    }


    public interface LoginTaskCallBack{
        void OnLoginComplete(String id);
    }
}