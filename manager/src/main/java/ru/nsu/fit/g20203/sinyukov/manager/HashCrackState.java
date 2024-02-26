package ru.nsu.fit.g20203.sinyukov.manager;

import java.util.ArrayList;
import java.util.List;

public class HashCrackState {

    public enum HashCrackStatus {
        IN_PROGRESS, READY, ERROR
    }

    private HashCrackStatus status = HashCrackStatus.IN_PROGRESS;
    private final List<String> results = new ArrayList<>();

    public synchronized boolean error() {
        if (HashCrackStatus.IN_PROGRESS == status) {
            status = HashCrackStatus.ERROR;
            return true;
        }
        return false;
    }

    public synchronized void addResults(List<String> results) {
        if (HashCrackStatus.ERROR == status) {
            return;
        }
        this.results.addAll(results);
        if (!results.isEmpty() && HashCrackStatus.IN_PROGRESS == status) {
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
        return String.format("%s[%s, %s]", HashCrackState.class.getSimpleName(), status, results);
    }
}
