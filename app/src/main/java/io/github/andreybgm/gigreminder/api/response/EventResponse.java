package io.github.andreybgm.gigreminder.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EventResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("categories")
    private List<String> categories;

    @SerializedName("site_url")
    private String url;

    @SerializedName("dates")
    private List<EventDate> dates;

    @SerializedName("place")
    private Place place;

    @SerializedName("title")
    private String title;

    @SerializedName("short_title")
    private String shortTitle;

    public int getId() {
        return id;
    }

    public List<String> getCategories() {
        return categories;
    }

    public String getUrl() {
        return url;
    }

    public List<EventDate> getDates() {
        return dates;
    }

    public Place getPlace() {
        return place;
    }

    public String getTitle() {
        return title;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public static class EventDate {
        @SerializedName("start")
        private long start;

        @SerializedName("end")
        private long end;

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }
    }

    public static class Place {
        @SerializedName("id")
        private int id;

        public int getId() {
            return id;
        }
    }
}
