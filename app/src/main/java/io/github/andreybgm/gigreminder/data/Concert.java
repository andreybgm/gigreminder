package io.github.andreybgm.gigreminder.data;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

public class Concert {
    @NonNull
    private final String id;

    @NonNull
    private final String apiCode;

    @NonNull
    private final Artist artist;

    @NonNull
    private final Location location;

    @NonNull
    private final Date date;

    @NonNull
    private final String place;

    @NonNull
    private final String url;

    @NonNull
    private final String imageUrl;

    public Concert(Builder builder) {
        this.id = builder.id;
        this.apiCode = builder.apiCode;
        this.artist = builder.artist;
        this.location = builder.location;
        this.url = builder.url;
        this.date = builder.date;
        this.imageUrl = builder.imageUrl;
        this.place = builder.place;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getApiCode() {
        return apiCode;
    }

    @NonNull
    public Artist getArtist() {
        return artist;
    }

    @NonNull
    public Location getLocation() {
        return location;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    @NonNull
    public String getPlace() {
        return place;
    }

    @Override
    public String toString() {
        return "Concert{" +
                "id='" + id + '\'' +
                ", apiCode='" + apiCode + '\'' +
                ", artist=" + artist +
                ", location=" + location +
                ", url='" + url + '\'' +
                ", date=" + date +
                ", imageUrl='" + imageUrl + '\'' +
                ", place='" + place + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Concert concert = (Concert) o;

        if (!id.equals(concert.id)) return false;
        if (!apiCode.equals(concert.apiCode)) return false;
        if (!artist.equals(concert.artist)) return false;
        if (!location.equals(concert.location)) return false;
        if (!url.equals(concert.url)) return false;
        if (!date.equals(concert.date)) return false;
        if (!imageUrl.equals(concert.imageUrl)) return false;
        return place.equals(concert.place);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + apiCode.hashCode();
        result = 31 * result + artist.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + imageUrl.hashCode();
        result = 31 * result + place.hashCode();
        return result;
    }

    public boolean equalsIgnoreId(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Concert concert = (Concert) o;

        if (!apiCode.equals(concert.apiCode)) return false;
        if (!artist.equals(concert.artist)) return false;
        if (!location.equals(concert.location)) return false;
        if (!url.equals(concert.url)) return false;
        if (!date.equals(concert.date)) return false;
        if (!imageUrl.equals(concert.imageUrl)) return false;
        return place.equals(concert.place);

    }

    public static class Builder {
        private final String id;
        private final String apiCode;
        private final Artist artist;
        private final Location location;
        private String url;
        private Date date;
        private String imageUrl;
        private String place;

        public Builder(String apiCode, Artist artist, Location location) {
            this(UUID.randomUUID().toString(), apiCode, artist, location);
        }

        public Builder(String id, String apiCode, Artist artist, Location location) {
            this.id = id;
            this.apiCode = apiCode;
            this.artist = artist;
            this.location = location;
        }

        public Concert build() {
            return new Concert(this);
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder date(Date date) {
            this.date = date;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder place(String place) {
            this.place = place;
            return this;
        }
    }
}
