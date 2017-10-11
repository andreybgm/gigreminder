package io.github.andreybgm.gigreminder.api.response;

import com.google.gson.annotations.SerializedName;

public class PlaceResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("short_title")
    private String shortTitle;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getShortTitle() {
        return shortTitle;
    }
}
