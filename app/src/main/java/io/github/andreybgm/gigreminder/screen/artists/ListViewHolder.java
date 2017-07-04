package io.github.andreybgm.gigreminder.screen.artists;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Artist;

class ListViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.text_name)
    TextView nameView;

    private final Drawable defaultBackground;
    private final Drawable selectionBackground;

    public ListViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        this.defaultBackground = itemView.getBackground();
        this.selectionBackground = itemView.getContext().getDrawable(
                R.drawable.activated_background);
    }

    public void bind(Artist artist, boolean actionModeOn, boolean selected) {
        nameView.setText(artist.getName());

        if (actionModeOn) {
            itemView.setActivated(selected);
            itemView.setBackground(selectionBackground);
        } else {
            itemView.setActivated(false);
            itemView.setBackground(defaultBackground);
        }
    }
}
