package pt.ulisboa.tecnico.locmess.wifidirect;

import android.util.Log;

import java.io.IOException;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;

/**
 * Created by goncalo on 25-03-2017.
 */

public class WifiDirectThread extends Thread {

    private static final String LOG_TAG = WifiDirectThread.class.getSimpleName();
    private boolean exit = false;
    private WifiDirectServerWorkerThread.Callback callback;
    private int port;

    public WifiDirectThread(WifiDirectServerWorkerThread.Callback callback, int port) {
        super();
        this.callback = callback;
        this.port = port;
    }
    @Override
    public void run() {
        SimWifiP2pSocketServer serverSocket = null;

            try {
                serverSocket = new SimWifiP2pSocketServer(port);
                while (!exit) {
                    /**
                     * Create a server socket and wait for client connections. This
                     * call blocks until a connection is accepted from a client
                     */

                    SimWifiP2pSocket client = serverSocket.accept();

                    // Delegate work to another thread
                    Thread t = new WifiDirectServerWorkerThread(client, callback);
                    t.start();
                }

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

    public void exit() {
        exit = true;
        interrupt();
    }
}
