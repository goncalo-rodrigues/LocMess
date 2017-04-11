package pt.ulisboa.tecnico.locmess.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import pt.ulisboa.tecnico.locmess.R;

/**
 * Created by nca on 20-03-2017.
 */

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {
    private List<String> data;
    private LocationsAdapter.Callback callback;

    public LocationsAdapter(List<String> list) {
        super();
        this.data = list;
    }

    public LocationsAdapter(List<String> list, LocationsAdapter.Callback callback) {
        this(list);
        this.callback = callback;
    }

    @Override
    public LocationsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.locations_list_item, parent, false);

        return new LocationsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final LocationsAdapter.ViewHolder holder, final int position) {
        String item = data.get(position);
        holder.loc_tv.setText(item);

        holder.remove_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null)
                    callback.onRemoveClicked(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<String> data) {
        this.data = data;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageButton remove_bt;
        public TextView loc_tv;


        public ViewHolder(View v) {
            super(v);
            remove_bt = (ImageButton) v.findViewById(R.id.locations_item_remove);
            loc_tv = (TextView) v.findViewById(R.id.locations_item_value);
        }
    }

    public interface Callback {
        void onRemoveClicked(int position);
    }
}
