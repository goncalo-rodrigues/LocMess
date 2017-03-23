package pt.ulisboa.tecnico.locmess.data.entities;

import java.util.Date;
import java.util.List;

/**
 * Created by goncalo on 23-03-2017.
 */

public class MuleMessage extends Message {
    private List<MuleMessageFilter> filters;
    private int hops = 0;

    public MuleMessage(int id, String messageText, String author, String location, Date startDate, Date endDate, List<MuleMessageFilter> filters, int hops) {
        super(id, messageText, author, location, startDate, endDate);
        this.filters = filters;
        this.hops = hops;
    }

    public List<MuleMessageFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<MuleMessageFilter> filters) {
        this.filters = filters;
    }

    public int getHops() {
        return hops;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }
}
