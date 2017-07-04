package io.github.andreybgm.gigreminder.screen.locations;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Location;

public class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {
    private final Context context;
    private LocationsUiModel uiModel;
    private final LocationsPresenter presenter;

    public ListAdapter(Context context, LocationsUiModel uiModel, LocationsPresenter presenter) {
        this.context = context;
        this.uiModel = uiModel;
        this.presenter = presenter;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_location, parent, false);

        return new ListViewHolder(view, presenter);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        Location location = uiModel.getLocations().get(position);
        holder.bind(location, position, uiModel);
    }

    @Override
    public int getItemCount() {
        return uiModel.getLocations().size();
    }

    public void changeDataSet(LocationsUiModel uiModel) {
        this.uiModel = uiModel;
        notifyDataSetChanged();
    }
}
