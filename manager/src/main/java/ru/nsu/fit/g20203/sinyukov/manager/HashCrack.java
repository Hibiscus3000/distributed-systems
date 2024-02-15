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
        if (HashCrackStatus.IN_PROGRESS == status) {
            status = HashCrackStatus.ERROR;
        }
    }

    public synchronized void addResults(List<String> results) {
        results.addAll(results);
        if (HashCrackStatus.IN_PROGRESS == status) {
            status = HashCrackStatus.READY;
        }
    }

    public synchronized HashCrackStatus getStatus() {
        return status;
    }

    public synchronized List<String> getResults() {
        return results;
    }
}
