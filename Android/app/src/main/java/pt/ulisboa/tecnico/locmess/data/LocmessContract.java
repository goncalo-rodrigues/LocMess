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

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MessageTable.TABLE_NAME + " (" +
                    MessageTable.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    MessageTable.COLUMN_NAME_CONTENT + " TEXT," +
                    MessageTable.COLUMN_NAME_AUTHOR + " TEXT," +
                    MessageTable.COLUMN_NAME_LOCATION + " TEXT," +
                    MessageTable.COLUMN_NAME_STARTDATE + " TEXT," +
                    MessageTable.COLUMN_NAME_ENDDATE + " TEXT);"
            +
            "CREATE TABLE " + CreatedMessageTable.TABLE_NAME + " (" +
                    CreatedMessageTable.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    CreatedMessageTable.COLUMN_NAME_CONTENT + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_AUTHOR + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_LOCATION + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_STARTDATE + " TEXT," +
                    CreatedMessageTable.COLUMN_NAME_ENDDATE + " TEXT);"
            +
            "CREATE TABLE " + MuleMessageTable.TABLE_NAME + " (" +
                    MuleMessageTable.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    MuleMessageTable.COLUMN_NAME_CONTENT + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_AUTHOR + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_LOCATION + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_STARTDATE + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_ENDDATE + " TEXT," +
                    MuleMessageTable.COLUMN_NAME_HOPS + " INTEGER);"
            +
            "CREATE TABLE " + MessageFilter.TABLE_NAME + " (" +
                    MessageFilter.COLUMN_NAME_MESSAGEID + " INTEGER," +
                    "FOREIGN KEY(" + MessageFilter.COLUMN_NAME_MESSAGEID + ") REFERENCES " +
                    MessageFilter.TABLE_NAME + "(" + MuleMessageTable.COLUMN_NAME_ID + "), " +
                    MessageFilter.COLUMN_NAME_KEY + " TEXT," +
                    MessageFilter.COLUMN_NAME_VALUE  + " TEXT," +
                    MessageFilter.COLUMN_NAME_BLACKLISTED + " BOOLEAN," +
                    "PRIMARY KEY (" + MessageFilter.COLUMN_NAME_MESSAGEID + ","
                    + MessageFilter.COLUMN_NAME_KEY + "," + MessageFilter.COLUMN_NAME_VALUE + "));"
            +
            "CREATE TABLE " + LocationTable.TABLE_NAME + " (" +
                    LocationTable.COLUMN_NAME_LOCATION + " TEXT PRIMARY KEY);";


    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MessageTable.TABLE_NAME;
}