package io.github.andreybgm.gigreminder.screen.concertdetails;

import android.support.annotation.NonNull;

import io.github.andreybgm.gigreminder.data.Concert;
import io.github.andreybgm.gigreminder.screen.base.UiModel;
import io.github.andreybgm.gigreminder.utils.Optional;

public class ConcertDetailsUiModel implements UiModel {
    private final boolean loading;
    private final boolean loadingError;
    @NonNull
    private final Optional<Concert> concert;
    @NonNull
    private final String concertId;
    @NonNull
    private final String linkToOpen;

    public static ConcertDetailsUiModel createDefault(@NonNull String concertId) {
        return Builder.create()
                .concertId(concertId)
                .build();
    }

    private ConcertDetailsUiModel(Builder builder) {
        this.loading = builder.loading;
        this.loadingError = builder.loadingError;
        this.concert = builder.concert;
        this.concertId = builder.concertId;
        this.linkToOpen = builder.linkToOpen;
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
    public String getLinkToOpen() {
        return linkToOpen;
    }

    @NonNull
    public Optional<Concert> getConcert() {
        return concert;
    }

    @NonNull
    public String getConcertId() {
        return concertId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConcertDetailsUiModel that = (ConcertDetailsUiModel) o;

        if (loading != that.loading) return false;
        if (loadingError != that.loadingError) return false;
        if (!concert.equals(that.concert)) return false;
        if (!concertId.equals(that.concertId)) return false;
        return linkToOpen.equals(that.linkToOpen);

    }

    @Override
    public int hashCode() {
        int result = (loading ? 1 : 0);
        result = 31 * result + (loadingError ? 1 : 0);
        result = 31 * result + concert.hashCode();
        result = 31 * result + concertId.hashCode();
        result = 31 * result + linkToOpen.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ConcertDetailsUiModel{" +
                "loading=" + loading +
                ", loadingError=" + loadingError +
                ", concert=" + concert +
                ", concertId='" + concertId + '\'' +
                ", linkToOpen='" + linkToOpen + '\'' +
                '}';
    }

    public static class Builder {
        private boolean loading;
        private boolean loadingError;
        @NonNull
        private Optional<Concert> concert;
        @NonNull
        private String concertId = "";
        @NonNull
        private String linkToOpen = "";

        public Builder(ConcertDetailsUiModel model) {
            this.loading = model.loading;
            this.loadingError = model.loadingError;
            this.concert = model.concert;
            this.concertId = model.concertId;
            this.linkToOpen = model.linkToOpen;
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder() {
            concert = Optional.empty();
        }

        public Builder loading(boolean loading) {
            this.loading = loading;
            return this;
        }

        public Builder loadingError(boolean loadingError) {
            this.loadingError = loadingError;
            return this;
        }

        public Builder concert(@NonNull Concert concert) {
            this.concert = Optional.of(concert);
            return this;
        }

        public Builder concertId(@NonNull String concertId) {
            this.concertId = concertId;
            return this;
        }

        public Builder linkToOpen(@NonNull String linkToOpen) {
            this.linkToOpen = linkToOpen;
            return this;
        }

        public ConcertDetailsUiModel build() {
            return new ConcertDetailsUiModel(this);
        }
    }
}
