package io.github.andreybgm.gigreminder.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchResponse {

    @SerializedName("results")
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public static class Result {
        @SerializedName("id")
        private int id;

        @SerializedName("first_image")
        private Image firstImage;

        public int getId() {
            return id;
        }

        public Image getFirstImage() {
            return firstImage;
        }
    }

    public static class Image {
        @SerializedName("thumbnails")
        private Thumbnail thumbnails;

        public Thumbnail getThumbnails() {
            return thumbnails;
        }
    }

    public static class Thumbnail {
        @SerializedName("640x384")
        private String img640x384;

        public String getImg640x384() {
            return img640x384;
        }
    }
}
