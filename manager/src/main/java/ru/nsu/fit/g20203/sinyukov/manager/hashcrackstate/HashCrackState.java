package ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate;

import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.nsu.fit.g20203.sinyukov.lib.IdentifiableByRequest;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Document("states")
public class HashCrackState implements IdentifiableByRequest {

    public enum HashCrackStatus {
        IN_PROGRESS, READY, ERROR
    }

    private final UUID id;
    private HashCrackStatus status;
    private final Set<String> results;

    @PersistenceCreator
    public HashCrackState(UUID id, HashCrackStatus status, Set<String> results) {
        this.id = id;
        this.status = status;
        this.results = results;
    }

    public HashCrackState(UUID id) {
        this(id, HashCrackStatus.IN_PROGRESS, new HashSet<>());
    }
    
    public synchronized boolean error() {
        if (HashCrackStatus.IN_PROGRESS == status) {
            status = HashCrackStatus.ERROR;
            return true;
        }
        return false;
    }

    public synchronized void addResults(Set<String> results) {
        if (HashCrackStatus.ERROR == status) {
            return;
        }
        this.results.addAll(results);
        if (!this.results.isEmpty()) {
            status = HashCrackStatus.READY;
        }
    }

    public synchronized void setReady() {
        if (HashCrackStatus.ERROR != status) {
            status = HashCrackStatus.READY;
        }
    }

    public synchronized HashCrackStatus getStatus() {
        return status;
    }

    public synchronized Set<String> getResults() {
        return results;
    }

    @Override
    public UUID getRequestId() {
        return id;
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
