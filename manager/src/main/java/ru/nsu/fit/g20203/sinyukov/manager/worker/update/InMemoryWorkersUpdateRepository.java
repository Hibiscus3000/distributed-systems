package ru.nsu.fit.g20203.sinyukov.manager.worker.update;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Primary
@Repository
@PropertySource("classpath:application.yml")
public class InMemoryWorkersUpdateRepository implements WorkersUpdateRepository {

    private final int numberOfWorkers;

    private final Map<UUID, Integer> workerUpdates = new HashMap<>();

    public InMemoryWorkersUpdateRepository(@Value("${workers.count}") int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    @Override
    public void create(UUID id) {
        workerUpdates.put(id, 0);
    }

    @Override
    public void update(UUID id) {
        if (!workerUpdates.containsKey(id)) {
            throw new IllegalArgumentException(String.format("Given UUID %s is not present in the workers updates repository", id));
        }
        workerUpdates.compute(id, (uuid, currentUpdateCount) -> currentUpdateCount + 1);
    }

    @Override
    public boolean areAllWorkersFinished(UUID id) {
        return numberOfWorkers == workerUpdates.get(id);
    }
}
