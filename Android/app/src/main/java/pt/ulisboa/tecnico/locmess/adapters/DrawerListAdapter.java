package pt.ulisboa.tecnico.locmess.adapters;

import android.content.Context;
import android.media.Image;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pt.ulisboa.tecnico.locmess.R;

/**
 * Created by goncalo on 21-03-2017.
 */

public class DrawerListAdapter extends RecyclerView.Adapter<DrawerListAdapter.ViewHolder> {

    private List<DrawerItem> items;
    private Callback callback;
    private int selected = -1;

    public DrawerListAdapter(List<DrawerItem> list, int selectedPos) {
        super();
        this.items = list;
        this.selected = selectedPos;
    }

    public DrawerListAdapter(List<DrawerItem> list, int selectedPos, DrawerListAdapter.Callback callback) {
        this(list, selectedPos);
        this.callback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.drawer_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Context ctx = holder.itemView.getContext();
        DrawerItem item = items.get(position);
        holder.title_tv.setText(item.desc);
        holder.icon_img.setImageResource(item.image_id);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onItemClick(holder.getAdapterPosition());
                }
            }
        });

        if (position == selected) {
            holder.itemView.setBackgroundColor(ctx.getResources().getColor(R.color.colorLightGray));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon_img;
        public TextView title_tv;


        public ViewHolder(View v) {
            super(v);
            icon_img = (ImageView) v.findViewById(R.id.drawer_list_item_img);
            title_tv = (TextView) v.findViewById(R.id.drawer_list_item_desc);
        }
    }

    public static class DrawerItem {
        public String desc;
        public @DrawableRes int image_id;

        public DrawerItem(String desc,@DrawableRes int image_id) {
            this.desc = desc;
            this.image_id = image_id;
        }
    }

    public interface Callback {
        void onItemClick(int position);
    }
}
