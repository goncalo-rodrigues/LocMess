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
import java.net.MalformedURLException;
import java.net.URL;

import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 26-03-2017.
 */

public class RegisterTask extends AsyncTask<String, String,String> {
    private RegisteTaskCallBack callback;
    private String result;
    NetworkGlobalState globalState;
    //private static final String URL_SERVER = "http://requestb.in/16z80wa1";

    private static final String URL_SERVER = "http://locmess.duckdns.org";

    public RegisterTask(RegisteTaskCallBack ltcb, Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }

    @Override
    protected String doInBackground(String... params) {
        String username = params[0];
        String password = params[1];
        String response = "";
        String id = "";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        try {
            jsoninputs.put("username", username);
            jsoninputs.put("password", password);

        //open the conection to the server and send
        URL url= null;

            url = new URL(URL_SERVER+"/signup");

            response=makeHTTPResquest(url,jsoninputs);

            JSONObject data = new JSONObject(response);

            if (data.opt("error") != null) {
                return  data.getString("error");
            }

            if (data.opt("session_id") == null)
                return "conetionError";

            id=data.getString("session_id");

            globalState.setUsername(username);
            globalState.setId(id);
            return id;

        }catch (JSONException e) {e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
            return "conetionError";
        }


        response = "|"+response+"|";
        return response;

    }

    @Override
    protected void onPostExecute(String result) {
        if (result.equals("conetionError"))
            callback.OnNoInternetConnection();
        else if(result.equals("alreadyExists"))
            callback.OnUserAlreadyExists(result);
        else
            callback.OnRegisterComplete(result);

        super.onPostExecute(result);
    }


    public interface RegisteTaskCallBack{
        void OnRegisterComplete(String id);
        void OnUserAlreadyExists(String error);
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