package io.github.andreybgm.gigreminder.screen.concerts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Concert;

public class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {
    private final Context context;
    private List<Concert> concerts;
    private OnItemClickListener itemClickListener;

    public ListAdapter(Context context, List<Concert> concerts) {
        this.context = context;
        this.concerts = concerts;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_concert, parent, false);

        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Concert concert = concerts.get(position);
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });
        holder.bind(concert);
    }

    @Override
    public int getItemCount() {
        return concerts.size();
    }

    public void changeDataSet(List<Concert> concerts) {
        this.concerts = concerts;
        notifyDataSetChanged();
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
