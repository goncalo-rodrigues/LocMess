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

import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 26-03-2017.
 */

public class SendMyLocationTask extends AsyncTask<Void, String,ArrayList<ReceivedMessage>>{
    private SendMyLocationsTaskCallBack callback;
    //private static final String URL_SERVER = "http://requestb.in/16z80wa1";
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;
    private ArrayList<Pair> gpsCoordiantes;
    private ArrayList<String> ssids;
    String errorToReturn = "";


    public SendMyLocationTask(SendMyLocationsTaskCallBack ltcb, Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }

    protected ArrayList<ReceivedMessage> doInBackground(ArrayList<Pair> gpsCoordiantes ,ArrayList<String> ssids){
        this.gpsCoordiantes = gpsCoordiantes;
        this.ssids = ssids;
        return doInBackground();
    }

    @Override
    protected ArrayList<ReceivedMessage> doInBackground(Void... params) {

        String result ="";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        JSONArray jsonCoordinates = new JSONArray(gpsCoordiantes);//todo check this
        JSONArray jsonSsids = new JSONArray(ssids);


        try {
            jsoninputs.put("session_id", globalState.getId());

            /*TODO verificar se o modo de cima funciona
            JSONObject coord;
            for(Pair coordinate : gpsCoordiantes){
                coord = new JSONObject();
                coord.put("lat",coordinate.first);
                coord.put("long",coordinate.second);
                jsonCoordinates.put(coord);
            }*/

            jsoninputs.put("gps",jsonCoordinates);
            jsoninputs.put("ssids",jsonSsids);


            //open the conection to the server and send
            URL url = new URL(URL_SERVER+"/send_locations");
            result= makeHTTPResquest(url,jsoninputs);

            //parse and get json elements, can be an array of locations or a error message

            JSONObject data = new JSONObject(result);
            String error = data.getString("error");

            if(error!=null && error.length()>0){
                errorToReturn = error;
                return null;
            }
            JSONArray messages =data.getJSONArray("messages");

            return retrieveMessagesFromJsonArray(messages);

        }catch (JSONException e) {e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
            errorToReturn = "conetionError";
            return null;
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

    /*{"username":"...", "location":"...", "start_date":XX, "end_date":YY, "content":"...",
     "filters":[{"key":"...", "value":"...", "is_whitelist": T/F }, ...]}*/
    private ArrayList<ReceivedMessage> retrieveMessagesFromJsonArray(JSONArray messages) throws JSONException {

        ArrayList<ReceivedMessage> result = new ArrayList<>();
        JSONObject message =null;
        String id;
        String username;
        String location;
        Date start_date;
        Date end_date;
        String content;


        for (int j=0;j<messages.length()-1;j++)
            message = messages.getJSONObject(j);
            if( message != null) {
                id = message.getString("id");
                username = message.getString("username");
                location = message.getString("location");
                start_date = new Date(message.getLong("start_date"));
                end_date = new Date(message.getLong("end_date"));
                content = message.getString("content");
                //TODO decoment when db ready
                result.add(new ReceivedMessage(id, content, username, location, start_date, end_date));
            }

        return result;
    }


    @Override
    protected void onPostExecute(ArrayList<ReceivedMessage> result) {
        //TODO see the possible errors and handle them
        if (result== null)
            callback.OnErrorResponse(errorToReturn);
        else
            callback.OnSendComplete(result);

        super.onPostExecute(result);
    }


    public interface SendMyLocationsTaskCallBack{
        void OnSendComplete(ArrayList<ReceivedMessage> locations);
        void OnErrorResponse(String error);
    }




}