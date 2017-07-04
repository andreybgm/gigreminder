package io.github.andreybgm.gigreminder.screen.artistimport.uievent;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.screen.base.event.BooleanMsgResult;
import io.github.andreybgm.gigreminder.utils.Optional;

public class SaveArtistsResult extends BooleanMsgResult {
    public static final SaveArtistsResult IN_PROGRESS =
            new SaveArtistsResult(State.IN_PROGRESS);
    public static final SaveArtistsResult SUCCESS =
            new SaveArtistsResult(State.SUCCESS);

    public static SaveArtistsResult error(int errorMsg) {
        return new SaveArtistsResult(State.ERROR, Optional.of(errorMsg));
    }

    private SaveArtistsResult(@NonNull State state) {
        super(state);
    }

    private SaveArtistsResult(@NonNull State state, @NonNull Optional<Integer> errorMsg) {
        super(state, errorMsg);
    }
}
