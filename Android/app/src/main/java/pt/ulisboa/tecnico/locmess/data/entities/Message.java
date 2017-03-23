package pt.ulisboa.tecnico.locmess.data.entities;

import java.util.Date;

/**
 * Created by goncalo on 23-03-2017.
 */

public class Message {

    private int id;
    private String messageText;
    private String author;
    private String location;
    private Date startDate;
    private Date endDate;


    public Message(int id, String messageText, String author, String location, Date startDate, Date endDate) {
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
}
