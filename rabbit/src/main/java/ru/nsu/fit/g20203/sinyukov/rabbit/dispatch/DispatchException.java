package ru.nsu.fit.g20203.sinyukov.rabbit.dispatch;

import java.util.UUID;

public class DispatchException extends Exception {

    public DispatchException(UUID id, Throwable cause) {
        super(id + ": Unable to dispatch", cause);
    }

    public DispatchException(String message, Throwable cause) {
        super("Unable to dispatch: " + message, cause);
    }
}
