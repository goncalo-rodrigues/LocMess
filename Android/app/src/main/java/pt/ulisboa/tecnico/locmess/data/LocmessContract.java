package pt.ulisboa.tecnico.locmess.data;

import android.provider.BaseColumns;

import pt.ulisboa.tecnico.locmess.data.entities.MuleMessage;

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

    }

    public static class LocationTable implements BaseColumns {
        public static final String TABLE_NAME = "locations";
        public static final String COLUMN_NAME_LOCATION = "location";
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

    public static final String SQL_CREATE_1 =
            "CREATE TABLE " + MessageTable.TABLE_NAME + " (" +
                    MessageTable.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    MessageTable.COLUMN_NAME_CONTENT + " TEXT," +
                    MessageTable.COLUMN_NAME_AUTHOR + " TEXT," +
                    MessageTable.COLUMN_NAME_LOCATION + " TEXT," +
                    MessageTable.COLUMN_NAME_STARTDATE + " TEXT," +
                    MessageTable.COLUMN_NAME_CENTRALIZED + " INTEGER," +
                    MessageTable.COLUMN_NAME_ENDDATE + " TEXT); ";
    public static final String SQL_CREATE_2 =
    "CREATE TABLE " + CreatedMessageTable.TABLE_NAME + " (" +
                    CreatedMessageTable.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    CreatedMessageTable.COLUMN_NAME_CONTENT + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_AUTHOR + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_LOCATION + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_STARTDATE + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_CENTRALIZED + " INTEGER," +
                    CreatedMessageTable.COLUMN_NAME_ENDDATE + " TEXT);";
    public static final String SQL_CREATE_3 =
            "CREATE TABLE " + MuleMessageTable.TABLE_NAME + " (" +
                    MuleMessageTable.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    MuleMessageTable.COLUMN_NAME_CONTENT + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_AUTHOR + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_LOCATION + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_STARTDATE + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_ENDDATE + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_HOPS + " INTEGER);";
    public static final String SQL_CREATE_4 =
            "CREATE TABLE " + MessageFilter.TABLE_NAME + " (" +
                    MessageFilter.COLUMN_NAME_MESSAGEID + " TEXT," +
                    MessageFilter.COLUMN_NAME_KEY + " TEXT," +
                    MessageFilter.COLUMN_NAME_VALUE  + " TEXT," +
                    MessageFilter.COLUMN_NAME_BLACKLISTED + " BOOLEAN," +
                    "FOREIGN KEY(" + MessageFilter.COLUMN_NAME_MESSAGEID + ") REFERENCES " +
                    MessageFilter.TABLE_NAME + "(" + MuleMessageTable.COLUMN_NAME_ID + "), " +
                    "PRIMARY KEY (" + MessageFilter.COLUMN_NAME_MESSAGEID + ","
                    + MessageFilter.COLUMN_NAME_KEY + "," + MessageFilter.COLUMN_NAME_VALUE + "));";
    public static final String SQL_CREATE_5 =
            "CREATE TABLE " + LocationTable.TABLE_NAME + " (" +
                    LocationTable.COLUMN_NAME_LOCATION + " TEXT PRIMARY KEY);";

    public static final String SQL_CREATE_6 =
            "CREATE TABLE " + ProfileKeyValue.TABLE_NAME + " (" +
                    ProfileKeyValue.COLUMN_NAME_KEY + " TEXT," +
                    ProfileKeyValue.COLUMN_NAME_VALUE  + " TEXT," +
                    "PRIMARY KEY ("
                    + MessageFilter.COLUMN_NAME_KEY + "," + MessageFilter.COLUMN_NAME_VALUE + "));";

    public static final String SQL_DELETE_1 =
            "DROP TABLE IF EXISTS " + MessageTable.TABLE_NAME;
    public static final String SQL_DELETE_2 =
            "DROP TABLE IF EXISTS " + MessageFilter.TABLE_NAME;
    public static final String SQL_DELETE_3 =
            "DROP TABLE IF EXISTS " + CreatedMessageTable.TABLE_NAME;
    public static final String SQL_DELETE_4 =
            "DROP TABLE IF EXISTS " + MuleMessageTable.TABLE_NAME;
    public static final String SQL_DELETE_5 =
            "DROP TABLE IF EXISTS " + LocationTable.TABLE_NAME;
    public static final String SQL_DELETE_6 =
            "DROP TABLE IF EXISTS " + ProfileKeyValue.TABLE_NAME;
}
