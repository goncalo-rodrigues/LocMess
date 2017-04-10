package pt.ulisboa.tecnico.locmess.wifidirect;

import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.ulisboa.tecnico.locmess.data.entities.MuleMessage;

/**
 * Created by goncalo on 10-04-2017.
 */

public class Policy {

    // returns true if should send message to device
    public boolean shouldSendToPeer(SimWifiP2pDevice device, MuleMessage message) {
        return true;
    }
}
