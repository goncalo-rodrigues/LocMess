package pt.ulisboa.tecnico.locmess.wifidirect;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;

/**
 * Created by goncalo on 25-03-2017.
 */

public class WifiDirectServerWorkerThread extends Thread {
    private static final String LOG_TAG = WifiDirectServerWorkerThread.class.getSimpleName();
    private final SimWifiP2pSocket client;
    private Callback callback;
    public static final int MESSAGE_TYPE_HANDSHAKE = 1;

    public WifiDirectServerWorkerThread(SimWifiP2pSocket client, @NonNull Callback callback) {
        super();
        this.client = client;
        this.callback = callback;
    }

    @Override
    public void run() {


        try {
            while(true) {
                InputStream inputstream = client.getInputStream();

                JsonReader reader = new JsonReader(new InputStreamReader(inputstream));
                Response response = null;
                try {
                    Request request = new Request(reader);
                    response = callback.onNewMessage(request);
                } catch (IOException ignored) {}


                if (response == null) {
                    return;
                } else {
                    OutputStream outputStream = client.getOutputStream();
                    outputStream.write(response.getJson().toString().getBytes());
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ocurred while receiving data: " +  e.getMessage());
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }



    public interface Callback {
        Response onNewMessage(Request message);
    }
}
