package io.github.andreybgm.gigreminder.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import io.github.andreybgm.gigreminder.data.Concert;

public class DefaultImageLoader implements ImageLoader {
    @Override
    public void loadConcertBackdrop(Context context, Concert concert, ImageView imageView) {
        Glide.with(context)
                .load(concert.getImageUrl())
                .centerCrop()
                .into(imageView);
    }
}
