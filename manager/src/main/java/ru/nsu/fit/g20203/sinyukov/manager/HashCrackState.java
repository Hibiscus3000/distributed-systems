package ru.nsu.fit.g20203.sinyukov.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class HashCrackState {

    public enum HashCrackStatus {
        IN_PROGRESS, READY, ERROR
    }

    private HashCrackStatus status = HashCrackStatus.IN_PROGRESS;
    private final Set<String> results = new HashSet<>();

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

    public synchronized boolean ready() {
        final boolean wasReady = HashCrackStatus.READY == status;
        status = HashCrackStatus.READY;
        return wasReady;
    }

    public synchronized HashCrackStatus getStatus() {
        return status;
    }

    public synchronized Set<String> getResults() {
        return results;
    }

    @Override
    public String toString() {
        return String.format("%s[%s, %s]", HashCrackState.class.getSimpleName(), status, results);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashCrackState that = (HashCrackState) o;
        return status == that.status && Objects.equals(results, that.results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, results);
    }
}
