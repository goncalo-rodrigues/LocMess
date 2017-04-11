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
 * Created by goncalo on 26-03-2017.
 */

public class WifiDirectClientThread extends Thread {
    private static final String LOG_TAG = WifiDirectClientThread.class.getSimpleName();
    String host;
    int port;
    Request message;
    Callback callback;
    public WifiDirectClientThread(String host, int port, Request message, @NonNull Callback callback) {
        super();
        this.host = host;
        this.port = port;
        this.message = message;
        this.callback = callback;
    }

    @Override
    public void run() {
        SimWifiP2pSocket socket = null;
        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket = new SimWifiP2pSocket(host, port);
//            socket.bind(null);
//            socket.connect((new InetSocketAddress(host, port)));

            while (true) {
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                outputStream.write(message.getJson().toString().getBytes());

                JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
                message = callback.onNewResponse(new Response(reader));
                if (message == null) {
                    // done!
                    return;
                }
            }



        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ocurred while sending data: " +  e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface Callback {
        Request onNewResponse(Response response);
    }
}
