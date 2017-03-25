package pt.ulisboa.tecnico.locmess.wifidirect;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by goncalo on 25-03-2017.
 */

public class WifiDirectThread extends Thread {

    private static final String LOG_TAG = WifiDirectThread.class.getSimpleName();
    private boolean exit = false;

    public WifiDirectThread() {
        super();

    }
    @Override
    public void run() {
        ServerSocket serverSocket = null;
        while (!exit) {
            try {

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */

                serverSocket = new ServerSocket(8000);
                Socket client = serverSocket.accept();

                // Delegate work to another thread
                Thread t = new WifiDirectClientThread(client);
                t.start();

            } catch (IOException e) {

                Log.e(LOG_TAG, "Error ocurred in wifi server: " +  e.getMessage());


            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

    }

    public void exit() {
        exit = true;
        interrupt();
    }
}
