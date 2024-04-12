package ru.nsu.fit.g20203.sinyukov.manager.worker.update;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryWorkersUpdateRepository implements WorkersUpdateRepository {

    private final int numberOfWorkers;

    private final Map<UUID, Set<Integer>> completedWorkerParts = new HashMap<>();

    public InMemoryWorkersUpdateRepository(@Value("${workers.count}") int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    @Override
    public void create(UUID id) {
        completedWorkerParts.put(id, new HashSet<>());
    }

    @Override
    public void update(UUID id, int workPartNumber) {
        if (!completedWorkerParts.containsKey(id)) {
            throw new IllegalArgumentException(String.format("Given UUID %s is not present in the workers updates repository", id));
        }
        completedWorkerParts.compute(id, (uuid, set) -> {
            set.add(workPartNumber);
            return set;
        });
    }

    @Override
    public boolean areAllWorkersFinished(UUID id) {
        return numberOfWorkers == completedWorkerParts.get(id).size();
    }
}
