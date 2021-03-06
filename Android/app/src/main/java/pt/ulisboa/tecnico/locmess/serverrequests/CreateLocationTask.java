package pt.ulisboa.tecnico.locmess.serverrequests;


import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import pt.ulisboa.tecnico.locmess.data.entities.FullLocation;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;


/**
 * Created by ant on 03-04-2017.
 */

public class CreateLocationTask extends AsyncTask<FullLocation, String,String>{
    private CreateLocationTaskCallBack callback;
    NetworkGlobalState globalState;
    String errorToReturn = "";

    public CreateLocationTask(CreateLocationTaskCallBack ltcb, Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }
    @Override
    protected String doInBackground(FullLocation... params) {
        if (params.length == 0) {
            return null;
        }
        FullLocation loc = params[0];
        if (loc == null) {
            return null;
        }


        String name = loc.getLocation();
        double lat = loc.getLatitude();
        double lon = loc.getLongitude();
        double radius = loc.getRadius();
        List<String> ssids = loc.getSsids();


        String result ="";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        JSONArray jsonSsids = new JSONArray(ssids);
        JSONObject jsongps = new JSONObject();


        try {
            jsoninputs.put("session_id", globalState.getId());
            jsoninputs.put("name",name);
            if (ssids.size()==0) {
                jsongps.put("lat", lat);
                jsongps.put("long", lon);
                jsongps.put("radius", radius);
                jsoninputs.put("gps", jsongps);
            }
            else
                jsoninputs.put("ssids",jsonSsids);

            //open the conection to the server and send
            result= CommonConnectionFunctions.makeHTTPResquest("create_location",jsoninputs);

            //parse and get json elements, can be an array of locations or a error message

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


    @Override
    protected void onPostExecute(String result) {
        //TODO see the possible errors and handle them
        if (result.equals("nok"))
            callback.onErrorResponse();

        else if (result.equals("conetionError"))
            callback.OnNoInternetConnection();

        else
            callback.createLocationComplete();

        super.onPostExecute(result);
    }


    public interface CreateLocationTaskCallBack{
        void createLocationComplete();
        void onErrorResponse();
        void OnNoInternetConnection();
    }




}