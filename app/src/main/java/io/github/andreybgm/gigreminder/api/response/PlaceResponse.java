package io.github.andreybgm.gigreminder.api.response;

import com.google.gson.annotations.SerializedName;

public class PlaceResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("short_title")
    private String shortTitle;

    @SerializedName("address")
    private String address;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public String getAddress() {
        return address;
    }
}
