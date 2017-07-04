package io.github.andreybgm.gigreminder.repository.api;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import io.github.andreybgm.gigreminder.GigApplication;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class MockingInterceptor implements Interceptor {

    private static final String LOG_TAG = MockingInterceptor.class.getSimpleName();
    private static final MediaType APPLICATION_JSON = MediaType.parse("application/json");
    private static final String RESPONSE_FOLDER = "api/response/";

    private static volatile boolean shouldReturnError;

    private static final Pattern SEARCH_PATTERN = Pattern.compile(
            ".*/search/\\?.*q=(.+).*&location=(.+)");
    private static final Pattern EVENTS_PATTERN = Pattern.compile(
            ".*/events/(\\d+)/\\?fields=id,categories,title,short_title,site_url,dates,place");
    private static final Pattern PLACES_PATTERN = Pattern.compile(
            ".*/places/(\\d+)/\\?fields=id,title,short_title,address");
    private static final Pattern LOCATIONS_PATTERN = Pattern.compile(
            ".*/(locations)/\\?fields=slug,name");

    private final PathHandler<String> pathHandler;

    public static Interceptor newInstance() {
        return new MockingInterceptor();
    }

    private MockingInterceptor() {
        pathHandler = PathHandler.Builder.<String>create()
                .patternHandler(SEARCH_PATTERN, PathHandler.Values.<String>create()
                        .value(RESPONSE_FOLDER + "search_AR-1_LC-1.json", "artist1", "lc1")
                        .value(RESPONSE_FOLDER + "search_AR-1_LC-2.json", "artist1", "lc2")
                        .value(RESPONSE_FOLDER + "search_AR-2_LC-1.json", "artist2", "lc1")
                        .value(RESPONSE_FOLDER + "search_empty_result.json", "artist2", "lc2")
                        .value(RESPONSE_FOLDER + "search_AR-3_LC-1.json", "artist3", "lc1")
                        .value(RESPONSE_FOLDER + "search_empty_result.json", "artist3", "lc2")
                        .value(RESPONSE_FOLDER + "search_AR-4_LC-2.json", "artist4", "lc2")
                        .value(RESPONSE_FOLDER + "search_empty_result.json", "artist4", "lc1")
                        .value(RESPONSE_FOLDER + "search_AR-5_LC-1.json", "artist5", "lc1")
                        .value(RESPONSE_FOLDER + "search_AR-5_LC-2.json", "artist5", "lc2")
                        .value(RESPONSE_FOLDER + "search_AR-LONG_LC-1.json",
                                "some%20very%20long%20artist%20name", "lc1")
                        .value(RESPONSE_FOLDER + "search_empty_result.json",
                                "some%20very%20long%20artist%20name", "lc2")
                        .value(RESPONSE_FOLDER + "nonexistent.json", "artist1", "lc0")
                        .value(RESPONSE_FOLDER + "search_AR-TRIPLE_LC-1.json",
                                "triple%20artist%20name", "lc1")
                        .value(RESPONSE_FOLDER + "search_empty_result.json",
                                "triple%20artist%20name", "lc2")
                        .value(RESPONSE_FOLDER + "search_AR-TRIPLE_LC-1.json",
                                "triple%20artist", "lc1")
                        .value(RESPONSE_FOLDER + "search_empty_result.json",
                                "triple%20artist", "lc2")
                )
                .patternHandler(EVENTS_PATTERN, PathHandler.Values.<String>create()
                        .value(RESPONSE_FOLDER + "events_1001_AR-1_LC-1.json", "1001")
                        .value(RESPONSE_FOLDER + "events_1002_AR-1_LC-2.json", "1002")
                        .value(RESPONSE_FOLDER + "events_1003_AR-2_LC-1.json", "1003")
                        .value(RESPONSE_FOLDER + "events_1004_AR-3_LC-1.json", "1004")
                        .value(RESPONSE_FOLDER + "events_1006_AR-4_LC-2.json", "1006")
                        .value(RESPONSE_FOLDER + "events_1007_AR-5_LC-1.json", "1007")
                        .value(RESPONSE_FOLDER + "events_1008_AR-5_LC-2.json", "1008")
                        .value(RESPONSE_FOLDER + "events_1009_AR-TRIPLE_LC-1.json", "1009")
                        .value(RESPONSE_FOLDER + "events_1005_AR-LONG_LC-1.json", "1005"))
                .patternHandler(PLACES_PATTERN, PathHandler.Values.<String>create()
                        .value(RESPONSE_FOLDER + "places_2001_LC-1.json", "2001")
                        .value(RESPONSE_FOLDER + "places_2002_LC-2.json", "2002"))
                .patternHandler(LOCATIONS_PATTERN, PathHandler.Values.<String>create()
                        .value(RESPONSE_FOLDER + "locations.json", "locations"))
                .build();
    }

    public static void setShouldReturnError(boolean shouldReturnError) {
        MockingInterceptor.shouldReturnError = shouldReturnError;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().toString();

        Log.d(LOG_TAG, String.format("Got the request %s", url));

        if (shouldReturnError) {
            return createErrorResponse(request, 500, "The synthetic network error");
        }

        String assetFileName = pathHandler.findValue(url);

        if (assetFileName != null) {
            return createResponseFromAsset(request, assetFileName);
        }

        throw new IllegalArgumentException(String.format("Cannot mock the request %s", url));
    }

    private Response createResponseFromAsset(Request request, String fileName) {
        AssetManager assets = GigApplication.getContext().getAssets();

        try (InputStream input = assets.open(fileName)) {
            return createSuccessResponse(request, input);
        } catch (IOException e) {
            return createErrorResponse(request, 500, e.getMessage());
        }
    }

    private Response createSuccessResponse(Request request, InputStream input) throws IOException {
        Buffer buffer = new Buffer().readFrom(input);

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(APPLICATION_JSON, buffer.size(), buffer))
                .build();
    }

    private Response createErrorResponse(Request request, int code, String message) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(message)
                .body(ResponseBody.create(APPLICATION_JSON, ""))
                .build();
    }
}
