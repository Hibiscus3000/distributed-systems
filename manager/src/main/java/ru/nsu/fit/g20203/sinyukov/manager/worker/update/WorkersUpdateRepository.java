package ru.nsu.fit.g20203.sinyukov.manager.worker.update;

import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkersUpdateRepository {

    void create(UUID id);

    void update(UUID id, int workPartNumber);

    boolean areAllWorkersFinished(UUID id);
}
