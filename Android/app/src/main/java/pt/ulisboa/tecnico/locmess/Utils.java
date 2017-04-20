package pt.ulisboa.tecnico.locmess;

import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by goncalo on 12-04-2017.
 */

public class Utils {

    private static int counter = 0;
    public static String buildMessageId(Context ctx, boolean centralized) {
        ByteBuffer buffer = ByteBuffer.allocate(1+128+10+4);
        buffer.put((byte) (centralized ? 0 : 1));
        byte[] session_id =( (NetworkGlobalState) ctx.getApplicationContext()).getId().getBytes();
        buffer.put(session_id);
        try {
        //"EEE MMM dd HH:mm:ss zzz yyyy"
            String timestamp = String.valueOf(( (NetworkGlobalState) ctx.getApplicationContext()).getSessionTimestamp().getTime() / 1000);
            buffer.put(String.format("%10s", timestamp).replace(' ', '0').getBytes("US-ASCII"));
            buffer.putInt(counter++);


            return new String(buffer.array(), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new String(buffer.array());
    }
}
