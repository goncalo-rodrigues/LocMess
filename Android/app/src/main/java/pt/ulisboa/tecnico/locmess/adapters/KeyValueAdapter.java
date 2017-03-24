package pt.ulisboa.tecnico.locmess.adapters;

import android.database.Cursor;
import android.media.Image;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import pt.ulisboa.tecnico.locmess.R;
import pt.ulisboa.tecnico.locmess.data.LocmessContract;
import pt.ulisboa.tecnico.locmess.data.entities.ProfileKeyValue;

/**
 * Created by goncalo on 17-03-2017.
 */

public class KeyValueAdapter extends RecyclerView.Adapter<KeyValueAdapter.ViewHolder> {
    private static final String LOG_TAG = MessagesAdapter.class.getSimpleName();
    private Cursor mData;
    private Callback callback;

    public KeyValueAdapter(Cursor data, Callback callback) {
        this.mData = data;
        this.callback = callback;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        mData.moveToPosition(position);
        final ProfileKeyValue item = new ProfileKeyValue(mData);
        holder.key_tv.setText(item.getKey());
        holder.value_tv.setText(item.getValue());

        holder.remove_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null)
                    callback.onRemoveClicked(holder.getAdapterPosition(), item);
                else
                    Log.e(LOG_TAG, "No callback defined for the adapter. Remove did nothing");

            }
        });



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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageButton remove_bt;
        public TextView key_tv;
        public TextView value_tv;


        public ViewHolder(View v) {
            super(v);
            remove_bt = (ImageButton) v.findViewById(R.id.profile_list_item_remove_bt);
            key_tv = (TextView) v.findViewById(R.id.profile_list_item_key_tv);
            value_tv = (TextView) v.findViewById(R.id.profile_list_item_value_tv);
        }
    }

    public interface Callback {
        void onRemoveClicked(int position, ProfileKeyValue item);
    }
}