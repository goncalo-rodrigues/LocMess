package pt.ulisboa.tecnico.locmess.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.R;

/**
 * Created by goncalo on 14-03-2017.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private List<Message> data;
    public MessagesAdapter(List<Message> list) {
        super();
        this.data = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.message_tv.setText(data.get(position).messageText);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message_tv;

        public ViewHolder(View v) {
            super(v);
            message_tv = (TextView) v.findViewById(R.id.item_message_preview);
        }
    }

    public static class Message {
        public String messageText;
        public String author;
        public Date date;

        public Message(String messageText, String author, Date date) {
            this.messageText = messageText;
            this.author = author;
            this.date = date;
        }
    }
}
