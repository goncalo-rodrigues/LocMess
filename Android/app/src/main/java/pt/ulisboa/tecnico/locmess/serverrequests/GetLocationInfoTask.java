package pt.ulisboa.tecnico.locmess.serverrequests;

import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import pt.ulisboa.tecnico.locmess.data.entities.FullLocation;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 06-04-2017.
 */

public class GetLocationInfoTask extends AsyncTask<Void, String, String>{
    private GetLocationInfoCallBack callback;
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;
    private ArrayList<String> ssids = new ArrayList<>();
    String location;
    Double lat;
    Double longitude;
    int radius;


    public GetLocationInfoTask(GetLocationInfoCallBack ltcb, Context context, String location){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
        this.location = location;
    }


    @Override
    protected String doInBackground(Void... params) {

        String result ="";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();

        try {
            jsoninputs.put("session_id", globalState.getId());
            jsoninputs.put("location",location);

            //open the conection to the server and send
            URL url = new URL(URL_SERVER+"/get_location_info");
            result= CommonConnectionFunctions.makeHTTPResquest(url,jsoninputs);

            //parse and get json elements, can be an array of locations or a error message
            JSONObject data = new JSONObject(result);

            if (data.opt("error") != null)
                return data.getString("error");

            if (data.opt("ssids") != null) {
                JSONArray jsonSsids = data.getJSONArray("ssids");
                for (int j = 0; j < jsonSsids.length() ; j++)
                    ssids.add(jsonSsids.getString(j));
            }

            if (data.opt("gps") != null) {
                JSONObject gpsJson = data.getJSONObject("gps");
                if (gpsJson.opt("lat") != null)
                    lat = gpsJson.getDouble("lat");
                if (gpsJson.opt("long") != null)
                    longitude = gpsJson.getDouble("long");
                if (gpsJson.opt("radius") != null)
                    radius = gpsJson.getInt("radius");
            }

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
                callback.OnGetLocationInfoComplete(flocation);
            } else {
                FullLocation flocation = new FullLocation(location,ssids);
                callback.OnGetLocationInfoComplete(flocation);
            }

        }
        else if(result.equals("conetionError"))
            callback.OnNoInternetConnection();
        else
            callback.OnGetInfoErrorResponse(result);

        super.onPostExecute(result);
    }


    public interface GetLocationInfoCallBack{
        void OnGetLocationInfoComplete(FullLocation flocation);
        void OnGetInfoErrorResponse(String error);
        void OnNoInternetConnection();
    }

}