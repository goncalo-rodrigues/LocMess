package pt.ulisboa.tecnico.locmess.serverrequests;

import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import pt.ulisboa.tecnico.locmess.PeriodicLocationService.TimestampedLocation;
import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 26-03-2017.
 */

public class GetMessagesTask extends AsyncTask<Void, String,String>{
    private GetMessagesCallBack callback;
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;
    ArrayList<TimestampedLocation> locations;
    Context context;
    ArrayList<ReceivedMessage> messagesList = new ArrayList<>();


    public GetMessagesTask(GetMessagesCallBack ltcb, Context context, ArrayList<TimestampedLocation> locations){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
        this.locations = locations;
        this.context =context;
    }


    @Override
    protected String doInBackground(Void... params) {
        String result ="";

        try{
            URL url = new URL(URL_SERVER+"/get_messages");
            JSONObject jsoninputs = createJsonMessage(locations);
            result= CommonConnectionFunctions.makeHTTPResquest(url,jsoninputs);

            //parse and get json elements, can be an array of locations or a error message
            JSONObject data = new JSONObject(result);

            if (data.opt("error") != null) {
                String error =  data.getString("error");
                return error;
            }

            saveMessagesFromJson(data);

            return "ok";

        }catch (JSONException e) {
            e.printStackTrace();
            return "JSONerror";
        }catch (IOException e) {
            e.printStackTrace();
            return "conetionError";
        }
    }


    @Override
    protected void onPostExecute(String result) {
        //TODO see the possible errors and handle them
        if (!result.equals("ok"))
            callback.OnGetMessagesError(result);
        else
            callback.OnGetMessagesComplete(messagesList);

        super.onPostExecute(result);
    }


    public void saveMessagesFromJson(JSONObject jsoninput) throws JSONException {
        JSONArray messages =jsoninput.getJSONArray("messages");
        JSONObject message =null;
        String id;
        String username;
        String location;
        Date start_date;
        Date end_date;
        String content;
        ReceivedMessage rm;

        for (int j=0;j<messages.length();j++) {
            message = messages.getJSONObject(j);
            if (message != null) {
                id = message.getString("id");
                username = message.getString("username");
                location = message.getString("location");
                start_date = new Date(message.getLong("start_date"));
                end_date = new Date(message.getLong("end_date"));
                content = message.getString("content");
                rm =new ReceivedMessage(id, content, username, location, start_date, end_date, false);
                messagesList.add(rm);
                //rm.save(context);
            }
        }
        return;
    }


    private JSONObject createJsonMessage(ArrayList<TimestampedLocation> locations) throws JSONException {
        JSONObject jsoninputs = new JSONObject();
        JSONArray jsonLocations = new JSONArray();

        jsoninputs.put("session_id", globalState.getId());

        JSONArray jsonSsids;
        JSONObject jsonlocation;
        for(TimestampedLocation location : locations){
            jsonlocation = new JSONObject();
            jsonlocation.put("lat",location.latitude);
            jsonlocation.put("long",location.longitude);
            jsonSsids = new JSONArray(location.ssids);
            jsonlocation.put("ssids",jsonSsids);
            jsonlocation.put("timestamp",location.timeStamp.getTime());
            jsonLocations.put(jsonlocation);
        }

        jsoninputs.put("locations",jsonLocations);
        return  jsoninputs;
    }


    public interface GetMessagesCallBack{
        void OnGetMessagesComplete(ArrayList<ReceivedMessage> messages);
        void OnGetMessagesError(String error);
    }

}