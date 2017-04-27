package pt.ulisboa.tecnico.locmess.serverrequests;


import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;


public class RemoveLocationTask extends AsyncTask<String, String,String>{
    private RemoveLocationTaskCallBack callback;
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;


    public RemoveLocationTask(RemoveLocationTaskCallBack ltcb, Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }


    @Override
    protected String doInBackground(String... params) {
        String name = params[0];
        String result ="";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();

        try {
            jsoninputs.put("session_id", globalState.getId());
            jsoninputs.put("name",name);

            //open the conection to the server and send
            URL url = new URL(URL_SERVER+"/remove_location");
            result= CommonConnectionFunctions.makeHTTPResquest(url,jsoninputs);

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
            callback.onNoInternetConnection();

        else
            callback.removeLocationComplete();

        super.onPostExecute(result);
    }


    public interface RemoveLocationTaskCallBack{
        void removeLocationComplete();
        void onErrorResponse();
        void onNoInternetConnection();
    }


}