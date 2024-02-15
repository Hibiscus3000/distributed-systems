package ru.nsu.fit.g20203.sinyukov.manager;

import java.util.ArrayList;
import java.util.List;

public class HashCrack {

    public enum HashCrackStatus {
        IN_PROGRESS, READY, ERROR
    }

    private HashCrackStatus status = HashCrackStatus.IN_PROGRESS;
    private final List<String> results = new ArrayList<>();

    public synchronized boolean timeout() {
        if (HashCrackStatus.IN_PROGRESS == status) {
            status = HashCrackStatus.ERROR;
        }
        return HashCrackStatus.ERROR == status;
    }

    public synchronized void addResults(List<String> results) {
        this.results.addAll(results);
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

    @Override
    public String toString() {
        return String.format("%s[%s, [%s]]", HashCrack.class.getName(), status, results);
    }
}
