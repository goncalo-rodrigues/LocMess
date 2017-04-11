package pt.ulisboa.tecnico.locmess.wifidirect;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by goncalo on 05-04-2017.
 */

public class Response {
    boolean success = false;

    public Response(boolean success) {
        this.success = success;
    }

    public Response(JsonReader reader) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "success":
                    success = reader.nextBoolean();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    public JSONObject getJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("success", success);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
