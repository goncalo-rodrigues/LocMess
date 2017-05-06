package pt.ulisboa.tecnico.locmess.serverrequests;

import android.content.Context;
import android.os.AsyncTask;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ulisboa.tecnico.locmess.data.entities.CreatedMessage;
import pt.ulisboa.tecnico.locmess.data.entities.ProfileKeyValue;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;


public class LoginTask extends AsyncTask<String, String,String> {
    private LoginTaskCallBack callback;
    NetworkGlobalState globalState;
    Context caller;


    public LoginTask(LoginTaskCallBack ltcb,Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
        caller = context;
    }


    @Override
    protected String doInBackground(String... params) {
        String username = params[0];
        String password = params[1];
        String response = "";
        String id = "";
        String key;
        String value;

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        try {
            jsoninputs.put("username", username);
            jsoninputs.put("password", password);

            //open the conection to the server and send
            response = CommonConnectionFunctions.makeHTTPResquest("login",jsoninputs);

            JSONObject data = new JSONObject(response);
            JSONArray filters = new JSONArray();
            JSONObject filter;
            ProfileKeyValue pkv;

            if (data.opt("error") != null) {
                 return  data.getString("error");
            }

            if (data.opt("session_id") == null)
                return "conetionError";

            id=data.getString("session_id");

            if (data.opt("filters") != null) {
                filters = data.getJSONArray("filters");
                for (int j = 0; j < filters.length(); j++) {
                    filter = filters.getJSONObject(j);
                    key = filter.getString("key");
                    value = filter.getString("value");
                    pkv = new ProfileKeyValue(key, value);
                    pkv.save(caller);
                }
            }

            if (data.opt("messages") != null) {
                saveMessagesFromJson(data);
            }

            int timestamp;
            if(data.opt("timestamp") != null){
                timestamp = data.getInt("timestamp");
                globalState.setSessionTimestamp(new Date(timestamp));
            }

            globalState.setId(id);
            globalState.setUsername(username);
            return id;
        }catch (JSONException e) {
            e.printStackTrace();
            return "json";
        }catch (IOException e) {
            e.printStackTrace();
            return "conetionError";
        }

    }


    @Override
    protected void onPostExecute(String result) {
        if (result.equals("conetionError"))
            callback.OnNoInternetConnection();
        else if(result.equals("wrongCredentials"))
            callback.OnWrongCredentials(result);
        else
        callback.OnLoginComplete(result);
        super.onPostExecute(result);
    }


    public interface LoginTaskCallBack{
        void OnLoginComplete(String id);
        void OnNoInternetConnection();
        void OnWrongCredentials(String error);
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
        CreatedMessage cm;

        for (int j=0;j<messages.length();j++) {
            message = messages.getJSONObject(j);
            if (message != null) {
                id = message.getString("id");
                username = message.getString("username");
                location = message.getString("location");
                start_date = new Date(message.getLong("start_date"));
                end_date = new Date(message.getLong("end_date"));
                content = message.getString("content");
                cm =new CreatedMessage(id, content, username, location, start_date, end_date, false);
                cm.save(caller);
                //rm.save(context);
            }
        }
        return;
    }

}