package ru.nsu.fit.g20203.sinyukov.manager.worker.update;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Primary
@Repository
public class DummyWorkersUpdateRepository implements WorkersUpdateRepository {
    @Override
    public void create(UUID id) {

    }

    @Override
    public void update(UUID id, int workPartNumber) {

    }

    @Override
    public boolean areAllWorkersFinished(UUID id) {
        return false;
    }
}
