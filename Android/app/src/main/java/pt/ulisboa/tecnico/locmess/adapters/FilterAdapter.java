package pt.ulisboa.tecnico.locmess.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

import pt.ulisboa.tecnico.locmess.R;



public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {
    private static final String LOG_TAG = MessagesAdapter.class.getSimpleName();
    private int MAX_PREVIEW_LEN = 100;
    private List<KeyValue> data;
    private int mExpandedPosition = -1;
    private Callback callback;
    public FilterAdapter(List<KeyValue> list) {
        super();
        this.data = list;
    }

    public FilterAdapter(List<KeyValue> list, Callback callback) {
        this(list);
        this.callback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.send_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        KeyValue item = data.get(position);
        holder.key_tv.setText(item.key);
        holder.value_tv.setText(item.value);
        holder.black_List_tb.setChecked(item.blacklisted);

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
        public ToggleButton black_List_tb;


        public ViewHolder(View v) {
            super(v);
            remove_bt = (ImageButton) v.findViewById(R.id.send_list_item_remove_bt);
            key_tv = (TextView) v.findViewById(R.id.send_list_item_key_tv);
            value_tv = (TextView) v.findViewById(R.id.send_list_item_value_tv);
            black_List_tb = (ToggleButton) v.findViewById(R.id.send_list_toggle_Button_Blacklist);
        }
    }

    public static class KeyValue {
        public String key;
        public String value;
        public boolean blacklisted;

        public KeyValue(String key, String value, boolean blacklisted) {
            this.key = key;
            this.value = value;
            this.blacklisted = blacklisted;
        }
    }

    public interface Callback {
        void onRemoveClicked(int position);
        void onChangeBlacklist(int Position);
    }
}