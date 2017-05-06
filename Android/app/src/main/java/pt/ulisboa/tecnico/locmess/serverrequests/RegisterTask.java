package pt.ulisboa.tecnico.locmess.serverrequests;


import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by ant on 26-03-2017.
 */

public class RegisterTask extends AsyncTask<String, String,String> {
    private RegisteTaskCallBack callback;
    private String result;
    NetworkGlobalState globalState;

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
            response = CommonConnectionFunctions.makeHTTPResquest("signup", jsoninputs);

            JSONObject data = new JSONObject(response);

            if (data.opt("error") != null) {
                return  data.getString("error");
            }

            if (data.opt("session_id") == null)
                return "conetionError";

            id=data.getString("session_id");

            int timestamp;
            if(data.opt("timestamp") != null){
                timestamp = data.getInt("timestamp");
                globalState.setSessionTimestamp(new Date(timestamp));
            }

            //TODO Colect the created messages from the server

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

}