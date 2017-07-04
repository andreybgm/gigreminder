package io.github.andreybgm.gigreminder.screen.concerts.uievent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.screen.base.event.BooleanResult;

public class LoadConcertsResult extends BooleanResult {
    public static final LoadConcertsResult IN_PROGRESS =
            new LoadConcertsResult(State.IN_PROGRESS, null, Collections.emptyList());

    public static LoadConcertsResult error(@NonNull Throwable error) {
        return new LoadConcertsResult(State.ERROR, error, Collections.emptyList());
    }

    public static LoadConcertsResult success(@NonNull List<Concert> concerts) {
        return new LoadConcertsResult(State.SUCCESS, null, concerts);
    }

    @NonNull
    private final List<Concert> concerts;

    private LoadConcertsResult(@NonNull State state, @Nullable Throwable error,
                               @NonNull List<Concert> concerts) {
        super(state, error);
        this.concerts = concerts;
    }

    @NonNull
    public List<Concert> getConcerts() {
        return concerts;
    }
}
