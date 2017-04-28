package pt.ulisboa.tecnico.locmess.data;

import android.provider.BaseColumns;

/**
 * Created by goncalo on 23-03-2017.
 */

public final class LocmessContract {

    // this class should not be instantiated
    private LocmessContract() {};

    public static class MessageTable implements BaseColumns {
        public static final String TABLE_NAME = "message";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_STARTDATE = "startdate";
        public static final String COLUMN_NAME_ENDDATE = "enddate";
        public static final String COLUMN_NAME_CENTRALIZED = "centralized";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }

    public static class CreatedMessageTable extends MessageTable {
        public static final String TABLE_NAME = "createdmessage";
    }


    public static class MuleMessageTable implements BaseColumns {
        public static final String TABLE_NAME = "mulemessage";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_STARTDATE = "startdate";
        public static final String COLUMN_NAME_ENDDATE = "enddate";
        public static final String COLUMN_NAME_HOPS = "hops";
        public static final String COLUMN_NAME_CENTRALIZED = "centralized";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }

    public static class LocationTable implements BaseColumns {
        public static final String TABLE_NAME = "locations";
        public static final String COLUMN_NAME_LOCATION = "location";
    }

    public static class SSIDSCacheTable implements BaseColumns {
        public static final String TABLE_NAME = "ssids_cache";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_LAST_SEEN = "last_seen";
    }

    public static class FullLocationTable implements BaseColumns {
        public static final String TABLE_NAME = "full_locations";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_LATITUDE = "gps_lat";
        public static final String COLUMN_NAME_LONGITUDE = "gps_lng";
        public static final String COLUMN_NAME_SSID = "wifi_ssid";
        public static final String COLUMN_NAME_RADIUS = "radius";
    }

    public static class MessageFilter implements BaseColumns {
        public static final String TABLE_NAME = "messagefilter";
        public static final String COLUMN_NAME_MESSAGEID = "messageid";
        public static final String COLUMN_NAME_KEY = "key";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_BLACKLISTED = "isblacklisted";
    }

    public static class ProfileKeyValue implements BaseColumns {
        public static final String TABLE_NAME = "profilekeyvalue";
        public static final String COLUMN_NAME_KEY = "key";
        public static final String COLUMN_NAME_VALUE = "value";
    }

    public static class PointTable implements BaseColumns {
        public static final String TABLE_NAME = "point";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_X = "x";
        public static final String COLUMN_NAME_Y = "y";
        public static final String COLUMN_NAME_NEXT = "next";
    }

    public static final String SQL_CREATE_MESSAGE_TBL =
            "CREATE TABLE " + MessageTable.TABLE_NAME + " (" +
                    MessageTable.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    MessageTable.COLUMN_NAME_CONTENT + " TEXT," +
                    MessageTable.COLUMN_NAME_AUTHOR + " TEXT," +
                    MessageTable.COLUMN_NAME_LOCATION + " TEXT," +
                    MessageTable.COLUMN_NAME_STARTDATE + " TEXT," +
                    MessageTable.COLUMN_NAME_CENTRALIZED + " INTEGER," +
                    MessageTable.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    MessageTable.COLUMN_NAME_ENDDATE + " TEXT); ";
    public static final String SQL_CREATE_CREATED_MESSAGE_TBL =
    "CREATE TABLE " + CreatedMessageTable.TABLE_NAME + " (" +
                    CreatedMessageTable.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    CreatedMessageTable.COLUMN_NAME_CONTENT + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_AUTHOR + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_LOCATION + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_STARTDATE + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    CreatedMessageTable.COLUMN_NAME_CENTRALIZED + " INTEGER," +
                    CreatedMessageTable.COLUMN_NAME_ENDDATE + " TEXT);";
    public static final String SQL_CREATE_MULE_MESSAGE_TBL =
            "CREATE TABLE " + MuleMessageTable.TABLE_NAME + " (" +
                    MuleMessageTable.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    MuleMessageTable.COLUMN_NAME_CONTENT + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_AUTHOR + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_LOCATION + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_STARTDATE + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    MuleMessageTable.COLUMN_NAME_ENDDATE + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_CENTRALIZED + " INTEGER DEFAULT 0," +
                    MuleMessageTable.COLUMN_NAME_HOPS + " INTEGER);";
    public static final String SQL_CREATE_MESSAGE_FILTER_TBL =
            "CREATE TABLE " + MessageFilter.TABLE_NAME + " (" +
                    MessageFilter.COLUMN_NAME_MESSAGEID + " TEXT," +
                    MessageFilter.COLUMN_NAME_KEY + " TEXT," +
                    MessageFilter.COLUMN_NAME_VALUE  + " TEXT," +
                    MessageFilter.COLUMN_NAME_BLACKLISTED + " BOOLEAN," +
                    "FOREIGN KEY(" + MessageFilter.COLUMN_NAME_MESSAGEID + ") REFERENCES " +
                    MessageFilter.TABLE_NAME + "(" + MuleMessageTable.COLUMN_NAME_ID + "), " +
                    "PRIMARY KEY (" + MessageFilter.COLUMN_NAME_MESSAGEID + ","
                    + MessageFilter.COLUMN_NAME_KEY + "," + MessageFilter.COLUMN_NAME_VALUE + "));";
    public static final String SQL_CREATE_LOCATION_TBL =
            "CREATE TABLE " + LocationTable.TABLE_NAME + " (" +
                    LocationTable.COLUMN_NAME_LOCATION + " TEXT PRIMARY KEY);";

    public static final String SQL_CREATE_PROFILE_KEYVAL_TBL =
            "CREATE TABLE " + ProfileKeyValue.TABLE_NAME + " (" +
                    ProfileKeyValue.COLUMN_NAME_KEY + " TEXT," +
                    ProfileKeyValue.COLUMN_NAME_VALUE  + " TEXT," +
                    "PRIMARY KEY ("
                    + MessageFilter.COLUMN_NAME_KEY + "," + MessageFilter.COLUMN_NAME_VALUE + "));";

    public static final String SQL_CREATE_POINT_TBL =
            "CREATE TABLE " + PointTable.TABLE_NAME + " (" +
                    PointTable.COLUMN_NAME_X + " REAL," +
                    PointTable.COLUMN_NAME_Y + " REAL," +
                    PointTable.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    PointTable.COLUMN_NAME_NEXT + " INTEGER," +
                    PointTable._ID + " INTEGER PRIMARY KEY," +
                    "FOREIGN KEY(" + PointTable.COLUMN_NAME_NEXT + ") REFERENCES " +
                    PointTable.TABLE_NAME + "(" + PointTable._ID + "));";

    public static final String SQL_CREATE_FULL_LOCATION_TBL =
            "CREATE TABLE " + FullLocationTable.TABLE_NAME + " (" +
                    FullLocationTable.COLUMN_NAME_LATITUDE + " REAL," +
                    FullLocationTable.COLUMN_NAME_LONGITUDE + " REAL," +
                    FullLocationTable.COLUMN_NAME_RADIUS + " REAL," +
                    FullLocationTable.COLUMN_NAME_SSID + " TEXT," +
                    FullLocationTable.COLUMN_NAME_LOCATION + " TEXT," +
                    FullLocationTable._ID + " INTEGER PRIMARY KEY);";

    public static final String SQL_CREATE_SSIDS_CACHE_TBL =
            "CREATE TABLE " + SSIDSCacheTable.TABLE_NAME + " (" +
                    SSIDSCacheTable.COLUMN_NAME_NAME + " TEXT," +
                    SSIDSCacheTable.COLUMN_NAME_LAST_SEEN + " INTEGER," +
                    SSIDSCacheTable._ID + " INTEGER PRIMARY KEY);";

    public static final String SQL_DELETE_MESSAGE_TBL =
            "DROP TABLE IF EXISTS " + MessageTable.TABLE_NAME;
    public static final String SQL_DELETE_MESSAGE_FILTER_TBL =
            "DROP TABLE IF EXISTS " + MessageFilter.TABLE_NAME;
    public static final String SQL_DELETE_CREATED_MESSAGE_TBL =
            "DROP TABLE IF EXISTS " + CreatedMessageTable.TABLE_NAME;
    public static final String SQL_DELETE_MULE_MESSAGE_TBL =
            "DROP TABLE IF EXISTS " + MuleMessageTable.TABLE_NAME;
    public static final String SQL_DELETE_LOCATION_TBL =
            "DROP TABLE IF EXISTS " + LocationTable.TABLE_NAME;
    public static final String SQL_DELETE_PROFILE_KEYVAL_TBL =
            "DROP TABLE IF EXISTS " + ProfileKeyValue.TABLE_NAME;
    public static final String SQL_DELETE_FULL_LOCATION_TBL =
            "DROP TABLE IF EXISTS " + FullLocationTable.TABLE_NAME;
    public static final String SQL_DELETE_POINT_TBL =
            "DROP TABLE IF EXISTS " + PointTable.TABLE_NAME;
    public static final String SQL_DELETE_SSIDS_CACHE_TBL =
            "DROP TABLE IF EXISTS " + SSIDSCacheTable.TABLE_NAME;
}
