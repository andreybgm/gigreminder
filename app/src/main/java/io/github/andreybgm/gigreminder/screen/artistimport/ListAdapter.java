package io.github.andreybgm.gigreminder.screen.artistimport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.andreybgm.gigreminder.R;

public class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {
    private final Context context;
    private OnItemClickListener itemClickListener;
    @NonNull
    private ArtistImportUiModel uiModel;

    public ListAdapter(@NonNull Context context, @NonNull ArtistImportUiModel uiModel) {
        this.context = context;
        this.uiModel = uiModel;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_import_artist, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        String name = uiModel.getArtists().get(position);
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });
        holder.bind(name, uiModel.isArtistSelected(position));
    }

    @Override
    public int getItemCount() {
        return uiModel.getArtists().size();
    }

    public void changeDataSet(@NonNull ArtistImportUiModel uiModel) {
        this.uiModel = uiModel;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.itemClickListener = clickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
