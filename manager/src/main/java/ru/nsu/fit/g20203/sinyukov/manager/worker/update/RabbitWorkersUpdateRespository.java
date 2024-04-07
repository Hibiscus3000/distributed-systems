package ru.nsu.fit.g20203.sinyukov.manager.worker.update;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Primary
@Repository
public class RabbitWorkersUpdateRespository extends InMemoryWorkersUpdateRepository {

    public RabbitWorkersUpdateRespository(@Value("${workers.count}") int numberOfWorkers) {
        super(numberOfWorkers);
    }

    @Override
    public boolean areAllWorkersFinished(UUID id) {
        return false; // because multiple patches can be returned for a single worker task 
    }
}
