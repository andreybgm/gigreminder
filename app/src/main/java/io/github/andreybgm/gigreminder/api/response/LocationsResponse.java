package io.github.andreybgm.gigreminder.api.response;

import com.google.gson.annotations.SerializedName;

public class LocationsResponse {
    @SerializedName("slug")
    private String apiCode;

    @SerializedName("name")
    private String name;

    public String getApiCode() {
        return apiCode;
    }

    public String getName() {
        return name;
    }
}
