package pt.ulisboa.tecnico.locmess.adapters;

import android.content.Context;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.R;

/**
 * Created by goncalo on 14-03-2017.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private static final String LOG_TAG = MessagesAdapter.class.getSimpleName();
    private int MAX_PREVIEW_LEN = 100;
    private List<Message> data;
    private int mExpandedPosition = -1;
    private Callback callback;
    public MessagesAdapter(List<Message> list) {
        super();
        this.data = list;
    }

    public MessagesAdapter(List<Message> list, Callback callback) {
        this(list);
        this.callback = callback;
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String text = data.get(position).messageText;
        String authorText = "by " + data.get(position).author;
        String locationText = "at " + data.get(position).location;
        String dateText = DateUtils.getRelativeTimeSpanString(data.get(position).date.getTime(),
                new Date().getTime(), 0L, DateUtils.FORMAT_ABBREV_ALL).toString();

        // expand
        final ViewGroup itemView = (ViewGroup) holder.itemView;
        final boolean isExpanded = position==mExpandedPosition;




        //holder.details_ll.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        itemView.setActivated(isExpanded);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1:holder.getAdapterPosition();
                TransitionManager.beginDelayedTransition(itemView);
                notifyDataSetChanged();
            }
        });

        holder.remove_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null)
                    callback.onRemoveClicked(holder.getAdapterPosition());
                else
                    Log.e(LOG_TAG, "No callback defined for the adapter. Remove did nothing");

            }
        });

        if (text.length() > MAX_PREVIEW_LEN && !isExpanded) {
            holder.message_tv.setText(text.substring(0, MAX_PREVIEW_LEN-3) + "...");
        } else {
            holder.message_tv.setText(text);
        }
        holder.author_tv.setText(authorText);
        holder.location_tv.setText(locationText);
        holder.date_tv.setText(dateText);


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Button remove_bt;
        public TextView message_tv;
        public RelativeLayout details_rl;
        public TextView author_tv;
        public TextView location_tv;
        public TextView date_tv;


        public ViewHolder(View v) {
            super(v);
            message_tv = (TextView) v.findViewById(R.id.main_item_message_tv);
            details_rl = (RelativeLayout) v.findViewById(R.id.main_item_details_rl);
            author_tv = (TextView) v.findViewById(R.id.main_item_author_tv);
            location_tv = (TextView) v.findViewById(R.id.main_item_location_tv);
            date_tv = (TextView) v.findViewById(R.id.main_item_date_tv);
            remove_bt = (Button) v.findViewById(R.id.main_item_remove_bt);
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

    public interface Callback {
        void onRemoveClicked(int position);
    }
}
