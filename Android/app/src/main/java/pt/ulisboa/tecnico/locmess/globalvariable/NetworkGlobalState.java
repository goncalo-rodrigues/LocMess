package pt.ulisboa.tecnico.locmess.globalvariable;



/**
 * Created by ant on 29-03-2017.
 */

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Date;

import pt.ulisboa.tecnico.locmess.R;
import pt.ulisboa.tecnico.locmess.Utils;

public class NetworkGlobalState extends Application{
    private static final String LOG_TAG = NetworkGlobalState.class.getSimpleName();
    private String id = null;
    private String username = null;
    private Date sessionTimestamp = null;
    private SharedPreferences settings;

    @Override
    public void onCreate() {
        super.onCreate();
        settings = getSharedPreferences(Utils.PREFS_NAME, 0);

        try {
            Utils.loadCert(R.raw.cert, this);
        } catch (Exception e){
            Log.e(LOG_TAG, "Unable to load cert: " + e.getMessage());
        }
    }

    public String getId() {
        if (id == null)
            return id = settings.getString(Utils.SESSION, null);
        return id;
    }

    public void setId(String id) {
        if(id != null)
            settings.edit().putString(Utils.SESSION, id).apply();

        this.id = id;
    }

    public String getUsername() {
        if(username == null)
            return username = settings.getString(Utils.USERNAME, null);

        return username;
    }

    public void setUsername(String username) {
        if(username != null)
            settings.edit().putString(Utils.USERNAME, username).apply();

        this.username = username;
    }

    public Date getSessionTimestamp() {
        if (sessionTimestamp == null)
            return sessionTimestamp = new Date(settings.getLong(Utils.SESSION_TS, -1));

        return sessionTimestamp;
    }

    public void setSessionTimestamp(Date sessionTimestamp) {
        if(sessionTimestamp != null)
            settings.edit().putLong(Utils.SESSION_TS, sessionTimestamp.getTime()).apply();

        this.sessionTimestamp = sessionTimestamp;
    }

    public void logout() {
        setId(null);
        setUsername(null);
        setSessionTimestamp(null);
        settings.edit().clear().commit();
    }
}
