package io.github.andreybgm.gigreminder.utils.retainedstate;

import android.os.Bundle;

public class RetainedStateUtils {

    private RetainedStateUtils() {
    }

    private static final String BUNDLE_RETAINED_CLIENT_ID =
            RetainedStateUtils.class.getName() + "_RETAINED_STATE";

    public static void putStateIdToBundle(Bundle outState, int id) {
        outState.putSerializable(BUNDLE_RETAINED_CLIENT_ID, id);
    }

    public static int restoreStateId(Bundle savedInstanceState) {
        int id;

        if (savedInstanceState == null) {
            id = RetainedStateHolder.EMPTY_ID;
        } else {
            id = savedInstanceState.getInt(BUNDLE_RETAINED_CLIENT_ID);
        }

        return id;
    }
}
