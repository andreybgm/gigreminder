package io.github.andreybgm.gigreminder.eventbus;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

public class BaseEventBusTest {

    private BaseEventBus eventBus;
    private TestObserver<BaseEvent> observer;
    private BaseEvent startEvent;
    private BaseEvent finishEvent;
    private Observable<BaseEvent> observable;

    @Before
    public void setUp() throws Exception {
        eventBus = new BaseEventBus();
        observable = eventBus.getObservable();
        observer = new TestObserver<>();
        startEvent = new StartEvent();
        finishEvent = new FinishEvent();
    }

    @Test
    public void subscribe() throws Exception {
        observable.subscribe(observer);

        observer.assertNoValues();
    }

    @Test
    public void sendEvents() throws Exception {
        observable.subscribe(observer);

        eventBus.sendBusEvent(startEvent);
        eventBus.sendBusEvent(finishEvent);

        observer.assertValues(startEvent, finishEvent);
    }

    @Test
    public void subscribeWhenThereWasEvent() throws Exception {
        eventBus.sendBusEvent(startEvent);

        observable.subscribe(observer);

        observer.assertValue(startEvent);
    }

    private static class StartEvent extends BaseEvent {
    }

    private static class FinishEvent extends BaseEvent {
    }
}