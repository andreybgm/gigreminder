package io.github.andreybgm.gigreminder.utils;

import android.content.Context;
import android.widget.ImageView;

import io.github.andreybgm.gigreminder.data.Concert;

public interface ImageLoader {
    void loadConcertBackdrop(Context context, Concert concert, ImageView imageView);
}
