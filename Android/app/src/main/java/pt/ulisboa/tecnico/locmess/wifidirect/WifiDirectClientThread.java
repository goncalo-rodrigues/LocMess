package pt.ulisboa.tecnico.locmess.wifidirect;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by goncalo on 25-03-2017.
 */

public class WifiDirectClientThread extends Thread {
    private static final String LOG_TAG = WifiDirectClientThread.class.getSimpleName();
    private final Socket client;

    public WifiDirectClientThread(Socket client) {
        super();
        this.client = client;
    }

    @Override
    public void run() {

        try {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            InputStream inputstream = client.getInputStream();
            int readBytes;
            while ((readBytes = inputstream.read(buffer, 0, buffer.length)) > 0) {
                result.write(buffer, 0, readBytes);
            }
            result.flush();
            Log.i(LOG_TAG, result.toString());

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ocurred while receiving data: " +  e.getMessage());
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
