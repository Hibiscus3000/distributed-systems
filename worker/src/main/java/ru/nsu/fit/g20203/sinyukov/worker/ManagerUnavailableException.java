package ru.nsu.fit.g20203.sinyukov.worker;

import org.springframework.http.HttpStatusCode;

public class ManagerUnavailableException extends Exception {

    public ManagerUnavailableException(String message, HttpStatusCode statusCode) {
        super(message + ". Status code: " + statusCode);
    }

    public ManagerUnavailableException(HttpStatusCode statusCode) {
        this("Manager unavailable", statusCode);
    }
}
