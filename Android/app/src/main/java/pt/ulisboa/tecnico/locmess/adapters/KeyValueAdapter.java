package pt.ulisboa.tecnico.locmess.adapters;

import android.media.Image;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
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

/**
 * Created by goncalo on 17-03-2017.
 */

public class KeyValueAdapter extends RecyclerView.Adapter<KeyValueAdapter.ViewHolder> {
    private static final String LOG_TAG = MessagesAdapter.class.getSimpleName();
    private List<KeyValue> data;
    private Callback callback;
    public KeyValueAdapter(List<KeyValue> list) {
        super();
        this.data = list;
    }

    public KeyValueAdapter(List<KeyValue> list, Callback callback) {
        this(list);
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
        KeyValue item = data.get(position);
        holder.key_tv.setText(item.key);
        holder.value_tv.setText(item.value);

        holder.remove_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null)
                    callback.onRemoveClicked(holder.getAdapterPosition());
                else
                    Log.e(LOG_TAG, "No callback defined for the adapter. Remove did nothing");

            }
        });



    }

    @Override
    public int getItemCount() {
        return data.size();
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

    public static class KeyValue {
        public String key;
        public String value;

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public interface Callback {
        void onRemoveClicked(int position);
    }
}