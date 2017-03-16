package pt.ulisboa.tecnico.locmess.adapters;

import android.content.Context;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.R;

/**
 * Created by goncalo on 14-03-2017.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private int MAX_PREVIEW_LEN = 100;
    private List<Message> data;
    private int mExpandedPosition = -1;
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String text = data.get(position).messageText;
        String authorText = "by " + data.get(position).author;
        String locationText = "at " + data.get(position).location;

        // expand
        final ViewGroup itemView = (ViewGroup) holder.itemView;
        final boolean isExpanded = position==mExpandedPosition;




        holder.details_ll.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        itemView.setActivated(isExpanded);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1:position;
                TransitionManager.beginDelayedTransition(itemView);
                notifyDataSetChanged();
            }
        });

        if (text.length() > MAX_PREVIEW_LEN && !isExpanded) {
            holder.message_tv.setText(text.substring(0, MAX_PREVIEW_LEN-3) + "...");
        } else {
            holder.message_tv.setText(text);
        }
        holder.author_tv.setText(authorText);
        holder.location_tv.setText(locationText);


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message_tv;
        public LinearLayout details_ll;
        public TextView author_tv;
        public TextView location_tv;


        public ViewHolder(View v) {
            super(v);
            message_tv = (TextView) v.findViewById(R.id.main_item_message_tv);
            details_ll = (LinearLayout) v.findViewById(R.id.main_item_details);
            author_tv = (TextView) v.findViewById(R.id.main_item_author_tv);
            location_tv = (TextView) v.findViewById(R.id.main_item_location_tv);
        }
    }

    public static class Message {
        public String messageText;
        public String author;
        public Date date;
        public String location; //TODO: location type

        public Message(String messageText, String author, Date date, String location) {
            this.messageText = messageText;
            this.author = author;
            this.date = date;
            this.location = location;
        }
    }
}
