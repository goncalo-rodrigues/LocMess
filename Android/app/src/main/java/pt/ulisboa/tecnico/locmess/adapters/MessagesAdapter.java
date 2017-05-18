package pt.ulisboa.tecnico.locmess.adapters;

import android.content.Context;
import android.database.Cursor;
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

import pt.ulisboa.tecnico.locmess.BaseMessageFragment;
import pt.ulisboa.tecnico.locmess.R;
import pt.ulisboa.tecnico.locmess.data.CustomCursorLoader;
import pt.ulisboa.tecnico.locmess.data.entities.CreatedMessage;
import pt.ulisboa.tecnico.locmess.data.entities.Message;
import pt.ulisboa.tecnico.locmess.data.entities.ReceivedMessage;

/**
 * Created by goncalo on 14-03-2017.
 */

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    private static final String LOG_TAG = MessagesAdapter.class.getSimpleName();
    private final int mType;
    private int MAX_PREVIEW_LEN = 100;
    private int mExpandedPosition = -1;
    private Callback callback;
    private Cursor mData;

    public MessagesAdapter(Cursor data,  Callback callback, int type) {
        this.callback = callback;
        this.mData = data;
        this.mType = type;
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
        mData.moveToPosition(position);
        final Message item;

        // MARTELO!
        if (mType == BaseMessageFragment.TYPE_ARG_CREATED) {
             item = new CreatedMessage(mData);
        } else {
             item = new ReceivedMessage(mData);
        }

        // Populate views

        String text = item.getMessageText();
        String authorText = "by " + item.getAuthor();
        String locationText = "at " + item.getLocation();
        String dateText = "";
        if (mType == BaseMessageFragment.TYPE_ARG_CREATED) {
            dateText = DateUtils.getRelativeTimeSpanString(item.getInsertDate().getTime(),
                    new Date().getTime(), 0L, DateUtils.FORMAT_ABBREV_ALL).toString();
        }


        // expand
        final ViewGroup itemView = (ViewGroup) holder.itemView;
        final boolean isExpanded = position==mExpandedPosition;

        itemView.setActivated(isExpanded);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1:holder.getAdapterPosition();
                TransitionManager.beginDelayedTransition(itemView);
                notifyDataSetChanged();
            }
        });

        // On click listener

        holder.remove_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null)
                    callback.onRemoveClicked(holder.getAdapterPosition(), item);
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
        if (mData == null) return 0; else return mData.getCount();
    }

    public Cursor getData() {
        return mData;
    }

    public void setData(Cursor data) {
        if (mData != null) mData.close();
        this.mData = data;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Button remove_bt;
        TextView message_tv;
        RelativeLayout details_rl;
        TextView author_tv;
        TextView location_tv;
        TextView date_tv;


        ViewHolder(View v) {
            super(v);
            message_tv = (TextView) v.findViewById(R.id.main_item_message_tv);
            details_rl = (RelativeLayout) v.findViewById(R.id.main_item_details_rl);
            author_tv = (TextView) v.findViewById(R.id.main_item_author_tv);
            location_tv = (TextView) v.findViewById(R.id.main_item_location_tv);
            date_tv = (TextView) v.findViewById(R.id.main_item_date_tv);
            remove_bt = (Button) v.findViewById(R.id.main_item_remove_bt);
        }
    }

    public interface Callback {
        void onRemoveClicked(int position, Message message);
    }
}
