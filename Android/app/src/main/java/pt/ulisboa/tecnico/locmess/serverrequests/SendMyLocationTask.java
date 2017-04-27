package pt.ulisboa.tecnico.locmess.serverrequests;


import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;
import pt.ulisboa.tecnico.locmess.PeriodicLocationService.TimestampedLocation;

/**
 * Created by ant on 26-03-2017.
 */

public class SendMyLocationTask extends AsyncTask<Void, String,String>{
    private SendMyLocationsTaskCallBack callback;
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;
    ArrayList<TimestampedLocation> locations;
    Context context;
    int numberMessages;

    public SendMyLocationTask(SendMyLocationsTaskCallBack ltcb, Context context, ArrayList<TimestampedLocation> locations){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
        this.locations = locations;
        this.context =context;
    }


    @Override
    protected String doInBackground(Void... params) {
        String result ="";

        try{
            URL url = new URL(URL_SERVER+"/send_locations");
            JSONObject jsoninputs = createJsonMessage(locations);
            result= CommonConnectionFunctions.makeHTTPResquest(url,jsoninputs);

            //parse and get json elements, can be the number of messages or a error message
            JSONObject data = new JSONObject(result);

            if (data.opt("error") != null) {
                String error =  data.getString("error");
                return error;
            }

            numberMessages =data.getInt("n_messages");
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
        if (!result.equals("ok"))
            callback.OnErrorResponse(result);
        else
            callback.OnSendComplete(numberMessages);

        super.onPostExecute(result);
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


    public interface SendMyLocationsTaskCallBack{
        void OnSendComplete(int numberMessages);
        void OnErrorResponse(String error);
    }

}