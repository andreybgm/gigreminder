package io.github.andreybgm.gigreminder.screen.locationchoice;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Location;

public class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {

    private final Context context;
    private List<Location> locations;
    private OnItemClickListener clickListener;

    public ListAdapter(Context context, List<Location> locations) {
        this.context = context;
        this.locations = locations;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_location_choice, parent, false);

        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Location location = locations.get(position);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(position);
            }
        });
        holder.bind(location);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void setClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void changeDataSet(List<Location> locations) {
        this.locations = locations;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
