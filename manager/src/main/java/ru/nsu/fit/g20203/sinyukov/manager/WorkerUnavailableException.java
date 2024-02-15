package ru.nsu.fit.g20203.sinyukov.manager;

import org.springframework.http.HttpStatusCode;

public class WorkerUnavailableException extends Exception {

    public WorkerUnavailableException(String message, HttpStatusCode statusCode) {
        super(message + ". Status code: " + statusCode);
    }

    public WorkerUnavailableException(HttpStatusCode statusCode) {
        this("Worker unavailable", statusCode);
    }
}
