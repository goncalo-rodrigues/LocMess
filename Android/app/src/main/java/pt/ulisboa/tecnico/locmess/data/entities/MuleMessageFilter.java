package pt.ulisboa.tecnico.locmess.data.entities;

/**
 * Created by goncalo on 23-03-2017.
 */

public class MuleMessageFilter {
    private int messageId;
    private String key;
    private String value;
    private boolean blackList;

    public MuleMessageFilter(int messageId, String key, String value, boolean blackList) {
        this.messageId = messageId;
        this.key = key;
        this.value = value;
        this.blackList = blackList;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isBlackList() {
        return blackList;
    }

    public void setBlackList(boolean blackList) {
        this.blackList = blackList;
    }
}
