package pt.ulisboa.tecnico.locmess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.IdRes;
import android.support.annotation.RawRes;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;
import pt.ulisboa.tecnico.locmess.globalvariable.NetworkGlobalState;

/**
 * Created by goncalo on 12-04-2017.
 */

public class Utils {

    private static SSLSocketFactory sslSocketFactory = null;
    private static int counter = 0;
    private static Certificate ca = null;

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

    public static void clearDatabase(Context ctx){
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(LocmessContract.SQL_DELETE_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_MESSAGE_FILTER_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_MULE_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_CREATED_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_LOCATION_TBL);
        db.execSQL(LocmessContract.SQL_DELETE_PROFILE_KEYVAL_TBL);

        db.execSQL(LocmessContract.SQL_CREATE_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_CREATED_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_MESSAGE_FILTER_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_MULE_MESSAGE_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_LOCATION_TBL);
        db.execSQL(LocmessContract.SQL_CREATE_PROFILE_KEYVAL_TBL);

    }

    public static void loadCert(@RawRes int certResourceId, Context ctx) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // Place the .crt file in /res/raw
        InputStream caInput = new BufferedInputStream(ctx.getResources().openRawResource(certResourceId));
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        sslSocketFactory = context.getSocketFactory();

    }

    public static SSLSocketFactory getSocketFactory() {
        return sslSocketFactory;
    }

    public static Certificate getCertificate() { return ca; }

    public static HttpsURLConnection openHTTPSConnection (URL url) throws IOException {
        HttpsURLConnection urlConnection =
                (HttpsURLConnection)url.openConnection();
        urlConnection.setSSLSocketFactory(sslSocketFactory);
        return urlConnection;
    }

}
