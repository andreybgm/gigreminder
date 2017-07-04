package io.github.andreybgm.gigreminder.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import io.github.andreybgm.gigreminder.R;
import io.github.andreybgm.gigreminder.data.Concert;

public class FakeImageLoader implements ImageLoader {
    @Override
    public void loadConcertBackdrop(Context context, Concert concert, ImageView imageView) {
        Glide.with(context)
                .load(R.drawable.fake_artist_image)
                .centerCrop()
                .into(imageView);
    }
}
