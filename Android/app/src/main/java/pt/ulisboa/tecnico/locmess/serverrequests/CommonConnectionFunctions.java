package pt.ulisboa.tecnico.locmess.serverrequests;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import pt.ulisboa.tecnico.locmess.Utils;

/**
 * Created by ant on 27-04-2017.
 */

public class CommonConnectionFunctions {
    public static String makeHTTPResquest(URL url, JSONObject jsoninputs) throws IOException {
        //TODO: uncomment this
//        HttpsURLConnection urlConnection = Utils.openHTTPSConnection(url);
        HttpURLConnection urlConnection= (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type","application/json");
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);
        urlConnection.connect();

        OutputStreamWriter out = new   OutputStreamWriter(urlConnection.getOutputStream());
        out.write(jsoninputs.toString());
        out.flush();
        out.close();

        BufferedReader buffer = new BufferedReader( new InputStreamReader(urlConnection.getInputStream(),"utf-8"));
        String result ="";
        String line ;
        while((line=buffer.readLine())!=null) {
            result += line;// +"\n";
        }

        return result;
    }
}
