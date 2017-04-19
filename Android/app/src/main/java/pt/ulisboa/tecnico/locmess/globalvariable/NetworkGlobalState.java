package pt.ulisboa.tecnico.locmess.globalvariable;



/**
 * Created by ant on 29-03-2017.
 */

import android.app.Application;

import java.util.Date;

import javax.crypto.SecretKey;

public class NetworkGlobalState extends Application{
    private String id ;
    private SecretKey communication_Key;
    private String username;
    private Date sessionTimestamp;

    public Date getSessionTimestamp() {
        if (sessionTimestamp == null) {
            return new Date(); // todo: fix this
        }
        return sessionTimestamp;
    }

    public void setSessionTimestamp(Date sessionTimestamp) {
        this.sessionTimestamp = sessionTimestamp;
    }

    public SecretKey getCommunication_Key() {
        return communication_Key;
    }

    public void setCommunication_Key(SecretKey communication_Key) {
        this.communication_Key = communication_Key;
    }

    public String getId() {
//        if (id == null) {
//            return "this.is.a.fake.128-byte.id.just.for.test.purposes.if.you.see.this.string.anywhere.please.report.to.someone.who.developed.the.app";
//        }
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
