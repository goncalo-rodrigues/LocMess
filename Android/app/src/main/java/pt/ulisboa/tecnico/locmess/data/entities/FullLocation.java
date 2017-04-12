package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.LocmessDbHelper;

/**
 * Created by goncalo on 06-04-2017.
 */

public class FullLocation extends Location{

    private double latitude;
    private double longitude;
    private double radius;
    private List<String> ssids = new ArrayList<>();

    public FullLocation(String location, double latitude, double longitude, double radius) {
        super(location);
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public FullLocation(String location, List<String> ssids) {
        super(location);
        this.ssids = new ArrayList<>(ssids);
    }

    public FullLocation(JsonReader reader) throws IOException {
        super("");
        String location = null;
        List<String> ssids = new ArrayList<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "location":
                    location = reader.nextString();
                    break;
                case "latitude":
                    latitude = reader.nextDouble();
                    break;
                case "longitude":
                    longitude = reader.nextDouble();
                    break;
                case "radius":
                    radius = reader.nextDouble();
                    break;
                case "ssids":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        ssids.add(reader.nextString());
                    }
                    reader.endArray();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        setLocation(location);
        this.ssids = ssids;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<String> getSsids() {
        return ssids;
    }

    @Override
    public void save(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        for (String ssid : ssids) {
            ContentValues values = new ContentValues();
            values.put(LocmessContract.FullLocationTable.COLUMN_NAME_LOCATION, getLocation());
            values.put(LocmessContract.FullLocationTable.COLUMN_NAME_LATITUDE, latitude);
            values.put(LocmessContract.FullLocationTable.COLUMN_NAME_LONGITUDE, longitude);
            values.put(LocmessContract.FullLocationTable.COLUMN_NAME_RADIUS, radius);
            values.put(LocmessContract.FullLocationTable.COLUMN_NAME_SSID, ssid);
            db.insert(LocmessContract.FullLocationTable.TABLE_NAME, null, values);
        }
        db.close();
    }

    @Override
    public void delete(Context ctx) {
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(LocmessContract.FullLocationTable.TABLE_NAME,
                LocmessContract.FullLocationTable.COLUMN_NAME_LOCATION + " = ?",
                new String[] {getLocation()});
        db.close();
    }

    public static FullLocation get(Context ctx, String location) {
        List<String> ssids = new ArrayList<>();
        LocmessDbHelper helper = new LocmessDbHelper(ctx);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor result =  db.query(LocmessContract.FullLocationTable.TABLE_NAME, null,
                LocmessContract.FullLocationTable.COLUMN_NAME_LOCATION + " = ?", new String[] {location},
                null, null, null);

        if (result.getCount() == 0) {
            result.close();
            return null;
        }
        result.moveToFirst();
        int lat_idx = result.getColumnIndexOrThrow(LocmessContract.FullLocationTable.COLUMN_NAME_LATITUDE);
        int lng_idx = result.getColumnIndexOrThrow(LocmessContract.FullLocationTable.COLUMN_NAME_LONGITUDE);
        int ssid_idx = result.getColumnIndexOrThrow(LocmessContract.FullLocationTable.COLUMN_NAME_SSID);
        int radius_idx = result.getColumnIndexOrThrow(LocmessContract.FullLocationTable.COLUMN_NAME_RADIUS);
        while (!result.isAfterLast()) {
            double lat = result.getDouble(lat_idx);
            double lng = result.getDouble(lng_idx);
            double rad = result.getDouble(radius_idx);
            String ssid = result.getString(ssid_idx);
            if (ssid == null || ssid.isEmpty()) {
                result.close();
                return new FullLocation(location, lat, lng, rad);
            }
            ssids.add(ssid);
            result.moveToNext();
        }
        result.close();
        //db.close();
        return  new FullLocation(location, ssids);
    }

    public JSONObject getJson() {
        JSONObject result = new JSONObject();
        try {
            result.put("location", getLocation());
            result.put("latitude", getLatitude());
            result.put("longitude", getLongitude());
            result.put("radius", getRadius());
            JSONArray sds = new JSONArray();
            for (String s : ssids) {
                sds.put(s);
            }
            result.put("ssids", sds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isGps() {
        return ssids == null || ssids.size() == 0;
    }

    public boolean isWifi() {
        return !isGps();
    }

    public boolean isInside(FullLocation anotherLoc) {
        // compare apples to apples and oranges to oranges
        if (anotherLoc.isGps() && this.isGps()) {
            double distance = computeDistance(getLatitude(), getLongitude(), anotherLoc.getLatitude(), anotherLoc.getLongitude());
            return distance < anotherLoc.getRadius();
        } else if (anotherLoc.isWifi() && this.isWifi()) {
            return !Collections.disjoint(this.ssids, anotherLoc.getSsids());
        }
        return false;
    }

    private double computeDistance(double lat1, double lon1, double lat2, double lon2) {
        // haversine formula taken from http://andrew.hedges.name/experiments/haversine/
        double R = 6371000; // earth's radius
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
//        dlon = lon2 - lon1
        double dlon = lon2 - lon1;
//        dlat = lat2 - lat1
        double dlat = lat2 - lat1;
//        a = (sin(dlat/2))^2 + cos(lat1) * cos(lat2) * (sin(dlon/2))^2
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) + Math.cos(lat1) * Math.cos(lat2) *  Math.sin(dlon/2) * Math.sin(dlon/2);
//        c = 2 * atan2( sqrt(a), sqrt(1-a) )
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//        d = R * c (where R is the radius of the Earth)
        double d = R * c;
        return d;
    }
}
