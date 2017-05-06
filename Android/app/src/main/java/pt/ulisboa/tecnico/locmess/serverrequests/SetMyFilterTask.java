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

public class SetMyFilterTask extends AsyncTask<String, String, String>{
    private SetMyFilterTaskCallBack callback;
    NetworkGlobalState globalState;
    String key;
    String value;


    public SetMyFilterTask(SetMyFilterTaskCallBack ltcb, Context context){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
    }


    @Override
    protected String doInBackground(String... params) {
        key = params[0];
        value = params[1];
        String result ="";

        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        JSONObject jsonfilter = new JSONObject();


        try {
            jsoninputs.put("session_id", globalState.getId());
            jsonfilter.put("key",key);
            jsonfilter.put("value",value);
            jsoninputs.put("filter",jsonfilter);

            //open the conection to the server and send
            result = CommonConnectionFunctions.makeHTTPResquest("set_my_filter", jsoninputs);

            //parse and get json elements, ok/nok
            JSONObject data = new JSONObject(result);
            String resp = data.getString("resp");

            return resp;

        }catch (JSONException e) {
            e.printStackTrace();
            return "nok";
        }catch (IOException e) {
            e.printStackTrace();
            return "connectionError";
        }

    }


    @Override
    protected void onPostExecute(String result) {
        if (result.equals("nok"))
            callback.onSetFilterErrorResponse();

        else if (result.equals("connectionError"))
            callback.OnNoInternetConnection();

        else
            callback.SetMyFilterComplete(key,value);

        super.onPostExecute(result);
    }


    public interface SetMyFilterTaskCallBack{
        void SetMyFilterComplete(String key,String value);
        void onSetFilterErrorResponse();
        void OnNoInternetConnection();
    }

}