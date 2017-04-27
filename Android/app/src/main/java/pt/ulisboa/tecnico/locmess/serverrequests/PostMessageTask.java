package pt.ulisboa.tecnico.locmess.serverrequests;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;


public class PostMessageTask extends AsyncTask<Void, String,String>{
    private PostMessageTaskCallBack callback;
    private static final String URL_SERVER = "http://locmess.duckdns.org";
    NetworkGlobalState globalState;
    //Variables to send
    private String username;
    private String location;
    private Date start_date;
    private Date end_date;
    private String content;
    private ArrayList<Pair> whitelisted;
    private ArrayList<Pair> blackListed;
    private String messageID;


    public PostMessageTask(PostMessageTaskCallBack ltcb, Context context,String username,
                           String location, Date start_date, Date end_date,
                           String content, ArrayList<Pair> whitelisted,ArrayList<Pair> blackListed,
                           String messageID){
        globalState = (NetworkGlobalState) context.getApplicationContext();
        callback = ltcb;
        this.username = username;
        this.location = location;
        this.start_date = start_date;
        this.end_date = end_date;
        this.content = content;
        this.whitelisted = whitelisted;
        this.blackListed = blackListed;
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
            for(Pair filter : whitelisted){
                jsonFilter = new JSONObject();
                jsonFilter.put("key",filter.first);
                jsonFilter.put("value",filter.second);
                jsonFilter.put("is_whitelist",true);
                jsonFilters.put(jsonFilter);
            }

            for(Pair filter : blackListed){
                jsonFilter = new JSONObject();
                jsonFilter.put("key",filter.first);
                jsonFilter.put("value",filter.second);
                jsonFilter.put("is_whitelist",false);
                jsonFilters.put(jsonFilter);
            }


            jsonMessage.put("filters",jsonFilters);
            jsoninputs.put("msg",jsonMessage);


            //open the conection to the server and send
            URL url = new URL(URL_SERVER+"/post_message");
            result = CommonConnectionFunctions.makeHTTPResquest(url,jsoninputs);

            //parse and get json elements, can be an array of locations or a error message
            JSONObject data = new JSONObject(result);
            String resp ="nok";
            if(data.opt("resp")!=null)
                resp = data.getString("resp");

            return resp;

        }catch (JSONException e) {
            e.printStackTrace();
            return "nok";
        }catch (IOException e) {
            e.printStackTrace();
            return "conetionError";
        }

    }


    @Override
    protected void onPostExecute(String result) {
        //TODO see the possible errors and handle them
        if (result.equals("nok"))
            callback.onErrorResponse();

        else if (result.equals("conetionError"))
            callback.OnNoInternetConnection();

        else
            callback.PostMessageComplete();

        super.onPostExecute(result);
    }


    public interface PostMessageTaskCallBack{
        void PostMessageComplete();
        void onErrorResponse();
        void OnNoInternetConnection();
    }
    
}