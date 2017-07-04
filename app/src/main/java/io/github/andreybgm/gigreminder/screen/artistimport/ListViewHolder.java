package io.github.andreybgm.gigreminder.screen.artistimport;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.andreybgm.gigreminder.R;

public class ListViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.check_box_name)
    CheckBox nameCheckBox;

    public ListViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, this.itemView);
    }

    public void bind(String name, boolean selected) {
        nameCheckBox.setText(name);
        nameCheckBox.setChecked(selected);
    }
}
