package io.github.andreybgm.gigreminder.screen.concertdetails.uievent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.screen.base.event.BooleanResult;
import io.github.andreybgm.gigreminder.utils.Optional;

public class LoadConcertResult extends BooleanResult {
    public static final LoadConcertResult IN_PROGRESS =
            new LoadConcertResult(State.IN_PROGRESS, null, Optional.empty());

    public static LoadConcertResult error(@NonNull Throwable error) {
        return new LoadConcertResult(State.ERROR, error, Optional.empty());
    }

    public static LoadConcertResult success(@NonNull Concert concert) {
        return new LoadConcertResult(State.SUCCESS, null, Optional.of(concert));
    }

    @NonNull
    private final Optional<Concert> concert;

    private LoadConcertResult(@NonNull State state, @Nullable Throwable error,
                              @NonNull Optional<Concert> concert) {
        super(state, error);
        this.concert = concert;
    }

    @NonNull
    public Optional<Concert> getConcert() {
        return concert;
    }
}
