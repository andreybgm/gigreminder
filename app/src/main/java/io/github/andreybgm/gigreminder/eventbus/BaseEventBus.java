package io.github.andreybgm.gigreminder.eventbus;

import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class BaseEventBus {
    private final Subject<BaseEvent> subject;
    private final ConnectableObservable<BaseEvent> observable;

    public BaseEventBus() {
        subject = PublishSubject.<BaseEvent>create().toSerialized();
        observable = subject.replay(1);
        observable.connect();
    }

    public void sendBusEvent(BaseEvent event) {
        subject.onNext(event);
    }

    public Observable<BaseEvent> getObservable() {
        return observable;
    }
}
