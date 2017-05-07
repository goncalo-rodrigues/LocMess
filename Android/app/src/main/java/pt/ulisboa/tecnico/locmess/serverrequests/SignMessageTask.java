package pt.ulisboa.tecnico.locmess.serverrequests;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Pair;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;

import pt.ulisboa.tecnico.locmess.Utils;
import pt.ulisboa.tecnico.locmess.data.entities.FullLocation;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessage;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessageFilter;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

public class SignMessageTask extends AsyncTask<Void, String,String> {
    private PostMessageTask.PostMessageTaskCallBack callback;
    NetworkGlobalState globalState;
    //Variables to send
    private String username;
    private FullLocation location;
    private Date start_date;
    private Date end_date;
    private String content;
    private ArrayList<MuleMessageFilter> filters;
    private String messageID;

    // FIXME: Just for testing purposes!!!
    private Context context;

    public SignMessageTask(Context context, String username,
                           FullLocation location, Date start_date, Date end_date,
                           String content, ArrayList<MuleMessageFilter> filters,
                           String messageID){

        globalState = (NetworkGlobalState) context.getApplicationContext();
//        callback = ltcb;
        this.username = username;
        this.location = location;
        this.start_date = start_date;
        this.end_date = end_date;
        this.content = content;
        this.filters = filters;
        this.messageID = messageID;

        // FIXME: Just for testing purposes!!!
        this.context = context;
    }


    @Override
    protected String doInBackground(Void... params) {
        //make the jason object to send
        JSONObject jsoninputs = new JSONObject();
        JSONObject jsonMessage = new JSONObject();
        JSONArray jsonFilters = new JSONArray();

        try {
            jsoninputs.put("session_id", globalState.getId());

            jsonMessage.put("id",messageID);
            jsonMessage.put("username", username);

            //TODO: Check if we want to keep returning the JSON on toString
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
            String result = CommonConnectionFunctions.makeHTTPResquest("sign_message", jsoninputs);

            //parse and get json elements, can be an array of locations or a error message
            JSONObject data = new JSONObject(result);
            String resp ="nok";

            if(data.opt("signed_msg")!=null)
                resp = data.getString("signed_msg");

            return resp;

        } catch (JSONException e) {
            e.printStackTrace();
            return "nok";
        } catch (IOException e) {
            e.printStackTrace();
            return "connectionError";
        }
    }


    @Override
    protected void onPostExecute(String result) {
        Toast t = null;

        if (result.equals("nok"))
            t = Toast.makeText(context, "Problem in server!", Toast.LENGTH_SHORT);

        else if (result.equals("connectionError"))
            t = Toast.makeText(context, "Connection error!", Toast.LENGTH_SHORT);

        else {
            String msgStr = messageID + username + location + start_date.getTime() + end_date.getTime() + content;

            for(MuleMessageFilter filter : filters)
                msgStr += filter.getKey() + filter.getValue() + (filter.isBlackList()? "0" : "1");

            try {
                byte[] msgBytes = msgStr.getBytes("UTF-8");
                byte[] receivedSig = Base64.decode(result, Base64.DEFAULT);

                Signature sig = Signature.getInstance("SHA256withRSA");
                sig.initVerify(Utils.getCertificate());
                sig.update(msgBytes);

                t = Toast.makeText(context, "Signature verification result: " + sig.verify(receivedSig), Toast.LENGTH_SHORT);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        t.show();

        super.onPostExecute(result);
    }


//    public interface SignMessageTaskCallBack{
//        void PostMessageComplete();
//        void onErrorResponse();
//        void OnNoInternetConnection();
//    }
}
