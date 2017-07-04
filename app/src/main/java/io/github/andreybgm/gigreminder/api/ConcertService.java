package io.github.andreybgm.gigreminder.api;

import java.util.List;

import io.github.andreybgm.gigreminder.api.response.EventResponse;
import io.github.andreybgm.gigreminder.api.response.LocationsResponse;
import io.github.andreybgm.gigreminder.api.response.PlaceResponse;
import io.github.andreybgm.gigreminder.api.response.SearchResponse;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ConcertService {
    @GET("search/?ctype=event&page_size=10")
    Observable<SearchResponse> search(@Query("q") String artistName,
                                      @Query("location") String location);

    @GET("events/{id}/?fields=id,categories,title,short_title,site_url,dates,place")
    Observable<EventResponse> eventDetails(@Path("id") int id);

    @GET("places/{id}/?fields=id,title,short_title,address")
    Observable<PlaceResponse> placeDetails(@Path("id") int id);

    @GET("locations/?fields=slug,name")
    Observable<List<LocationsResponse>> locations();
}
