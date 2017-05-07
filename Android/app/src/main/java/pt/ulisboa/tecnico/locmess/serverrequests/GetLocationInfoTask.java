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
import java.util.List;

import pt.ulisboa.tecnico.locmess.data.entities.FullLocation;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessageFilter;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 06-04-2017.
 */

public class GetLocationInfoTask extends AsyncTask<Void, String, String>{
    private GetLocationInfoCallBack callback;
    NetworkGlobalState globalState;

    // Variables to send
    private String username;
    String location;
    private Date start_date;
    private Date end_date;
    private String content;
    private List<MuleMessageFilter> filters;
    private String messageID;

    // To receive
    private ArrayList<String> ssids = new ArrayList<>();
    Double lat;
    Double longitude;
    int radius;
    String sig = null;


    public GetLocationInfoTask(GetLocationInfoCallBack ltcb, Context context, String username,
                               String location, Date start_date, Date end_date, String content,
                               List<MuleMessageFilter> filters, String messageID){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
        this.username = username;
        this.location = location;
        this.start_date = start_date;
        this.end_date = end_date;
        this.content = content;
        this.filters = filters;
        this.messageID = messageID;
    }


    @Override
    protected String doInBackground(Void... params) {

        String result ="";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        JSONObject jsonMessage = new JSONObject();
        JSONArray jsonFilters = new JSONArray();

        try {
            jsoninputs.put("session_id", globalState.getId());
            jsonMessage.put("id",messageID);
            jsonMessage.put("username", username);
            jsonMessage.put("location", location);
            jsonMessage.put("start_date", start_date.getTime());
            jsonMessage.put("end_date", end_date.getTime());
            jsonMessage.put("content",content);

            JSONObject jsonFilter;
            for(MuleMessageFilter filter : filters){
                jsonFilter = new JSONObject();
                jsonFilter.put("key",filter.getKey());
                jsonFilter.put("value",filter.getValue());
                jsonFilter.put("is_whitelist",!filter.isBlackList());
                jsonFilters.put(jsonFilter);
            }

            jsonMessage.put("filters",jsonFilters);
            jsoninputs.put("msg",jsonMessage);

            //open the conection to the server and send
            result= CommonConnectionFunctions.makeHTTPResquest("get_location_info", jsoninputs);

            //parse and get json elements, can be an array of locations or a error message
            JSONObject data = new JSONObject(result);

            if (data.opt("error") != null)
                return data.getString("error");

            boolean gotInformation = false;

            if (data.opt("ssids") != null) {
                gotInformation = true;
                JSONArray jsonSsids = data.getJSONArray("ssids");
                for (int j = 0; j < jsonSsids.length() ; j++)
                    ssids.add(jsonSsids.getString(j));
            }

            if (data.opt("gps") != null) {
                gotInformation = true;
                JSONObject gpsJson = data.getJSONObject("gps");
                if (gpsJson.opt("lat") != null)
                    lat = gpsJson.getDouble("lat");
                if (gpsJson.opt("long") != null)
                    longitude = gpsJson.getDouble("long");
                if (gpsJson.opt("radius") != null)
                    radius = gpsJson.getInt("radius");
            }

            if(data.opt("signed_msg") != null && gotInformation)
                sig = data.getString("signed_msg");

            return "ok";

        }catch (JSONException e) {e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
            return "conetionError";
        }
        //never reach here unless we get an error parsing the json
        return null;
    }


    @Override
    protected void onPostExecute(String result) {
        //TODO see the possible errors and handle them
        if ("ok".equals(result)) {
            if (lat != null) {
                FullLocation flocation = new FullLocation(location,lat,longitude,radius);
                callback.OnGetLocationInfoComplete(flocation, filters, sig);
            } else {
                FullLocation flocation = new FullLocation(location,ssids);
                callback.OnGetLocationInfoComplete(flocation, filters, sig);
            }

        }
        else if(result.equals("conetionError"))
            callback.OnNoInternetConnection();
        else
            callback.OnGetInfoErrorResponse(result);

        super.onPostExecute(result);
    }


    public interface GetLocationInfoCallBack{
        void OnGetLocationInfoComplete(FullLocation flocation, List<MuleMessageFilter>filters, String sig);
        void OnGetInfoErrorResponse(String error);
        void OnNoInternetConnection();
    }

}