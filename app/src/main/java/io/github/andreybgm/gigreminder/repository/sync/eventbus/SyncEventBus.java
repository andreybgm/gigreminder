package io.github.andreybgm.gigreminder.repository.sync.eventbus;

import io.github.andreybgm.gigreminder.eventbus.BaseEvent;
import io.github.andreybgm.gigreminder.eventbus.BaseEventBus;
import io.reactivex.Observable;

public class SyncEventBus extends BaseEventBus {
    private final static BaseEventBus eventBus = new BaseEventBus();

    public static Observable<BaseEvent> getEventBus() {
        return eventBus.getObservable();
    }

    public static void sendEvent(BaseEvent event) {
        eventBus.sendBusEvent(event);
    }
}
