package ru.nsu.fit.g20203.sinyukov.manager;

import java.util.ArrayList;
import java.util.List;

public class HashCrack {

    public enum HashCrackStatus {
        IN_PROGRESS, READY, ERROR
    }

    private HashCrackStatus status = HashCrackStatus.IN_PROGRESS;
    private final List<String> results = new ArrayList<>();

    public synchronized void timeout() {
        status = HashCrackStatus.ERROR;
    }

    public synchronized void addResult(String result) {
        results.add(result);
    }

    public synchronized HashCrackStatus getStatus() {
        return status;
    }

    public synchronized List<String> getResults() {
        return results;
    }
}
