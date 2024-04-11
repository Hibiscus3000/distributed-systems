package ru.nsu.fit.g20203.sinyukov.rabbit.dispatch;

import ru.nsu.fit.g20203.sinyukov.lib.IdentifiableByRequest;

public class Dispatchable<T extends IdentifiableByRequest> {

    private final T payload;
    private final Runnable successfulDispatchCallback;

    public Dispatchable(T payload, Runnable successfulDispatchCallback) {
        this.payload = payload;
        this.successfulDispatchCallback = successfulDispatchCallback;
    }

    public Dispatchable(T payload) {
        this(payload, null);
    }

    public T getPayload() {
        return payload;
    }

    public void dispatchedSuccessfully() {
        successfulDispatchCallback.run();
    }
}
