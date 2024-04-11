package ru.nsu.fit.g20203.sinyukov.rabbit.connection;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConnectionState {

    private boolean open;

    private final List<ConnectionObserver> observers = new ArrayList<>();

    public void connectionOpened() {
        open = true;
        for (var observer : observers) {
            observer.connectionOpened();
        }
    }

    public void connectionClosed() {
        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    public void addObserver(ConnectionObserver observer) {
        observers.add(observer);
    }
}
