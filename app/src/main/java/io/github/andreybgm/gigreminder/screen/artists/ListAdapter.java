package io.github.andreybgm.gigreminder.screen.artists;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Artist;

class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {

    private final Context context;
    private OnItemClickListener itemClickListener;
    private ArtistsUiModel uiModel;

    public ListAdapter(Context context, ArtistsUiModel uiModel) {
        this.context = context;
        this.uiModel = uiModel;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_artist, parent, false);

        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Artist artist = uiModel.getArtists().get(position);

        holder.itemView.setOnClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(artist, position);
            }
        });
        holder.itemView.setOnLongClickListener(view -> {
            if (itemClickListener != null) {
                itemClickListener.onItemLongClick(artist, position);
            }

            return true;
        });
        holder.bind(artist, uiModel.isActionModeOn(), uiModel.isArtistSelected(position));
    }

    @Override
    public int getItemCount() {
        return uiModel.getArtists().size();
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.itemClickListener = clickListener;
    }

    public void changeDataSet(ArtistsUiModel uiModel) {
        this.uiModel = uiModel;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Artist artist, int position);

        void onItemLongClick(Artist artist, int position);
    }
}
