package io.github.andreybgm.gigreminder.utils.retainedstate;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

@SuppressLint("UseSparseArrays")
public class RetainedStateHolder {
    public static final int EMPTY_ID = 0;

    private final Map<Integer, Object> idToState;
    private int lastClientId;
    private Map<Integer, StateProvider> idToStateProvider = new HashMap<>();

    public RetainedStateHolder() {
        lastClientId = EMPTY_ID;
        idToState = null;
    }

    public RetainedStateHolder(Object rawRetainedState) {
        HolderState retainedState;

        try {
            retainedState = (HolderState) rawRetainedState;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format(
                    "Expect %s", HolderState.class.getSimpleName()));
        }

        lastClientId = retainedState.lastClientId;
        idToState = retainedState.idToState;
    }

    public static RetainedStateHolder create(AppCompatActivity activity) {
        RetainedStateHolder stateHolder;
        Object state = activity.getLastCustomNonConfigurationInstance();

        if (state == null) {
            stateHolder = new RetainedStateHolder();
        } else {
            stateHolder = new RetainedStateHolder(state);
        }

        return stateHolder;
    }

    public int addProvider(StateProvider stateProvider) {
        return addProvider(EMPTY_ID, stateProvider);
    }

    public int addProvider(int existedId, StateProvider stateProvider) {
        int id;

        if (existedId == EMPTY_ID) {
            id = ++lastClientId;
        } else {
            id = existedId;
        }

        idToStateProvider.put(id, stateProvider);

        return id;
    }

    public Object createHolderState() {
        Map<Integer, Object> idToState = Observable.fromIterable(idToStateProvider.entrySet())
                .toMap(Map.Entry::getKey, entry -> entry.getValue().provideState())
                .blockingGet();

        return new HolderState(lastClientId, idToState);
    }

    public <T> T restoreState(int id) {
        if (idToState == null) {
            return null;
        }

        try {
            //noinspection unchecked
            return (T) idToState.getOrDefault(id, null);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public interface StateProvider {
        Object provideState();
    }

    private static class HolderState {
        private final int lastClientId;
        private final Map<Integer, Object> idToState;

        HolderState(int lastClientId, Map<Integer, Object> idToState) {
            this.lastClientId = lastClientId;
            this.idToState = idToState;
        }
    }
}
