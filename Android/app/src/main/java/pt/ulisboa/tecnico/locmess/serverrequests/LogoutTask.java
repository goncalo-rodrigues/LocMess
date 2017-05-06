package pt.ulisboa.tecnico.locmess.serverrequests;

import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;

import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 03-04-2017.
 */

public class LogoutTask extends AsyncTask<Void, String, String>{
    private LogoutCallBack callback;
    NetworkGlobalState globalState;


    public LogoutTask(LogoutCallBack ltcb, Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }


    @Override
    protected String doInBackground(Void... params) {
        String result ="";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();

        try {
            jsoninputs.put("session_id", globalState.getId());

            //open the conection to the server and send
            result = CommonConnectionFunctions.makeHTTPResquest("logout", jsoninputs);

            //parse and get json elements, ok/nok
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
        globalState.setUsername(null);

        if (result.equals("nok")) {
            callback.logoutErrorResponse();
        }
        else if (result.equals("conetionError")) {
            callback.OnLogoutNoInternetConnection();
        }

        else {
            globalState.setCommunication_Key(null);
            globalState.setId(null);
            callback.logoutComplete();
        }
        super.onPostExecute(result);
    }


    public interface LogoutCallBack{
        void logoutComplete();
        void logoutErrorResponse();
        void OnLogoutNoInternetConnection();
    }

}