package io.github.andreybgm.gigreminder.screen.locations;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Location;
import io.github.andreybgm.gigreminder.screen.locations.uievent.DeleteLocationEvent;

public class ListViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.text_name)
    TextView nameView;

    @BindView(R.id.button_delete)
    ImageButton deleteButton;

    private final LocationsPresenter presenter;

    public ListViewHolder(View itemView, LocationsPresenter presenter) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        this.presenter = presenter;
    }

    public void bind(Location location, int position, LocationsUiModel uiModel) {
        nameView.setText(location.getName());
        deleteButton.setOnClickListener(v -> presenter.sendUiEvent(
                DeleteLocationEvent.create(position, uiModel)
        ));
    }
}
