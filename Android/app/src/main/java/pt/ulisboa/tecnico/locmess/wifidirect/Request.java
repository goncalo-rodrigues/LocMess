package pt.ulisboa.tecnico.locmess.wifidirect;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pt.ulisboa.tecnico.locmess.data.entities.MuleMessage;

/**
 * Created by goncalo on 03-04-2017.
 */

public class Request {
    /* This indicates all requests possible and their codes */
    public final static int REQUEST_MULE_MESSAGE = 1; // used to ask someone to mule a message for us
    int id = 0;
    Object json;

    public Request(int id, JSONObject json) {
        this.id = id;
        this.json = json;
        if (json == null) {
            try {
                this.json = new JSONObject("{ 'test' : 'abc' }");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Request(JsonReader reader) throws IOException {


        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "id":
                    id = reader.nextInt();
                    break;
                case "content":
                    reader.beginObject();
                    json = getContentFromReader(reader);
                    reader.endObject();
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
            result.put("id", id);
            result.put("content", json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Object getContentFromReader(JsonReader reader) throws IOException {

        switch(id) {
            case REQUEST_MULE_MESSAGE:
                return new MuleMessage(reader);
            default:
                JSONObject result = new JSONObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    try {
                        result.put(name, reader.nextString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        reader.skipValue();
                    }
                }
                return result;
        }
    }

    public Object getContent() {
        return json;
    }
}
