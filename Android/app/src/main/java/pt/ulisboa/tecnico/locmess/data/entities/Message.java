package pt.ulisboa.tecnico.locmess.data.entities;

import android.content.Context;
import android.database.Cursor;

import java.util.Date;

import pt.ulisboa.tecnico.locmess.data.LocmessContract;

/**
 * Created by goncalo on 23-03-2017.
 */

public abstract class Message {

    private int id;
    private String messageText;
    private String author;
    private String location;
    private Date startDate;
    private Date endDate;

    public Message(final Cursor cursor) {
        int id_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageTable.COLUMN_NAME_ID);
        int txt_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageTable.COLUMN_NAME_CONTENT);
        int author_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageTable.COLUMN_NAME_AUTHOR);
        int location_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageTable.COLUMN_NAME_LOCATION);
        int stdate_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageTable.COLUMN_NAME_STARTDATE);
        int enddate_idx = cursor.getColumnIndexOrThrow(LocmessContract.MessageTable.COLUMN_NAME_ENDDATE);

        init(cursor.getInt(id_idx), cursor.getString(txt_idx), cursor.getString(author_idx), cursor.getString(location_idx)
                , new Date(cursor.getString(stdate_idx)), new Date(cursor.getString(enddate_idx)));
    }


    public Message(int id, String messageText, String author, String location, Date startDate, Date endDate) {
        init(id,  messageText, author, location, startDate, endDate);
    }

    private void init(int id, String messageText, String author, String location, Date startDate, Date endDate) {
        this.id = id;
        this.messageText = messageText;
        this.author = author;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public abstract void save(Context ctx);

    public abstract void delete(Context ctx);
}
