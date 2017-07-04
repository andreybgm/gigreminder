package io.github.andreybgm.gigreminder.screen.locationchoice;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Location;

public class ListViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.text_location)
    TextView locationTextView;

    public ListViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Location location) {
        locationTextView.setText(location.getName());
    }
}
