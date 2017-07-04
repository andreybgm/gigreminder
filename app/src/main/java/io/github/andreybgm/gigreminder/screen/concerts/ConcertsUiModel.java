package io.github.andreybgm.gigreminder.screen.concerts;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.screen.base.UiModel;
import io.github.andreybgm.gigreminder.utils.Optional;

public class ConcertsUiModel implements UiModel {

    public static ConcertsUiModel DEFAULT = Builder.create().build();

    private final boolean loading;
    private final boolean loadingError;
    @NonNull
    private final List<Concert> concerts;
    @NonNull
    private final Optional<Concert> concertToOpen;
    private final boolean syncing;

    public ConcertsUiModel(Builder builder) {
        this.loading = builder.loading;
        this.loadingError = builder.loadingError;
        this.concerts = builder.concerts;
        this.concertToOpen = builder.concertToOpen;
        this.syncing = builder.syncing;
    }

    public Builder copy() {
        return new Builder(this);
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isLoadingError() {
        return loadingError;
    }

    @NonNull
    public List<Concert> getConcerts() {
        return concerts;
    }

    @NonNull
    public Optional<Concert> getConcertToOpen() {
        return concertToOpen;
    }

    public boolean isSyncing() {
        return syncing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConcertsUiModel that = (ConcertsUiModel) o;

        if (loading != that.loading) return false;
        if (loadingError != that.loadingError) return false;
        if (syncing != that.syncing) return false;
        if (!concerts.equals(that.concerts)) return false;
        return concertToOpen.equals(that.concertToOpen);

    }

    @Override
    public int hashCode() {
        int result = (loading ? 1 : 0);
        result = 31 * result + (loadingError ? 1 : 0);
        result = 31 * result + concerts.hashCode();
        result = 31 * result + concertToOpen.hashCode();
        result = 31 * result + (syncing ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConcertsUiModel{" +
                "loading=" + loading +
                ", loadingError=" + loadingError +
                ", concerts=" + concerts +
                ", concertToOpen=" + concertToOpen +
                ", syncing=" + syncing +
                '}';
    }

    public static class Builder {
        private boolean loading;
        private boolean loadingError;
        @NonNull
        private List<Concert> concerts;
        @NonNull
        private Optional<Concert> concertToOpen;
        private boolean syncing;

        public static Builder create() {
            return new Builder();
        }

        public Builder() {
            concerts = Collections.emptyList();
            concertToOpen = Optional.empty();
        }

        public Builder(ConcertsUiModel model) {
            this.loading = model.loading;
            this.loadingError = model.loadingError;
            this.concerts = model.concerts;
            this.concertToOpen = model.concertToOpen;
            this.syncing = model.syncing;
        }

        public ConcertsUiModel build() {
            return new ConcertsUiModel(this);
        }

        public Builder loading(boolean loading) {
            this.loading = loading;
            return this;
        }

        public Builder loadingError(boolean loadingError) {
            this.loadingError = loadingError;
            return this;
        }

        public Builder concerts(@NonNull List<Concert> concerts) {
            this.concerts = concerts;
            return this;
        }

        public Builder concertToOpen(@NonNull Concert concert) {
            this.concertToOpen = Optional.of(concert);
            return this;
        }

        public Builder clearConcertToOpen() {
            concertToOpen = Optional.empty();
            return this;
        }

        public Builder syncing(boolean syncing) {
            this.syncing = syncing;
            return this;
        }
    }
}
