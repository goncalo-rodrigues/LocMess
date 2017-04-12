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
    Object content;
    JSONObject json;

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
                    getContentFromReader(reader);
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

    private void getContentFromReader(JsonReader reader) throws IOException {

        switch(id) {
            case REQUEST_MULE_MESSAGE:
                MuleMessage m = new MuleMessage(reader);
                content = m;
                json = m.getJson();
                break;
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
                content = result;
                json = result;
        }
    }

    @Override
    public String toString() {
        return getJson().toString();
    }

    public Object getContent() {
        return content;
    }
}
