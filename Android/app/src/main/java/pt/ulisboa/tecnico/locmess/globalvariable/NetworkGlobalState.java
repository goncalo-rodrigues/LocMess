package pt.ulisboa.tecnico.locmess.globalvariable;



/**
 * Created by ant on 29-03-2017.
 */

import android.app.Application;

import javax.crypto.SecretKey;

public class NetworkGlobalState extends Application{
    private String id;
    private SecretKey communication_Key;
    private String username;

    public SecretKey getCommunication_Key() {
        return communication_Key;
    }

    public void setCommunication_Key(SecretKey communication_Key) {
        this.communication_Key = communication_Key;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void logout(){
        id = null;
        communication_Key = null;
        username = null;
    }


}
