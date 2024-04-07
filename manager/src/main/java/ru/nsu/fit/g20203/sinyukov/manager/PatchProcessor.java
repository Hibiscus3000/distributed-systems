package ru.nsu.fit.g20203.sinyukov.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstaterepository.HashCrackStateRepository;
import ru.nsu.fit.g20203.sinyukov.manager.worker.update.WorkersUpdateRepository;

import java.util.Set;
import java.util.UUID;

@Component
public class PatchProcessor {

    private final Logger logger = LoggerFactory.getLogger(PatchProcessor.class);

    private final HashCrackStateRepository hashCrackStateRepository;
    private final WorkersUpdateRepository workersUpdateRepository;

    private final HashCrackTimer hashCrackTimer;

    public PatchProcessor(HashCrackStateRepository hashCrackStateRepository,
                          WorkersUpdateRepository workersUpdateRepository,
                          HashCrackTimer hashCrackTimer) {
        this.hashCrackStateRepository = hashCrackStateRepository;
        this.workersUpdateRepository = workersUpdateRepository;
        this.hashCrackTimer = hashCrackTimer;
    }

    public void process(HashCrackPatch patch) {
        final UUID id = patch.id();
        final Set<String> results = patch.results();

        updateHashCrack(id, results);
        logResults(id, results);
        if (workersUpdateRepository.areAllWorkersFinished(id) || !results.isEmpty())
            hashCrackTimer.cancelTimeout(id);
    }

    private void updateHashCrack(UUID id, Set<String> results) {
        final HashCrackState hashCrackState = hashCrackStateRepository.getHashCrack(id);
        hashCrackState.addResults(results);
        workersUpdateRepository.update(id);
        if (workersUpdateRepository.areAllWorkersFinished(id) && hashCrackState.ready()) {
            logger.info(String.format("%s: All workers have finished working, found results %s", id, hashCrackState.getResults()));
        }
    }

    private void logResults(UUID id, Set<String> results) {
        if (!results.isEmpty()) {
            logger.info(id + ": Worker found results: " + results);
        } else {
            logger.info(id + ": Worker found no results");
        }
    }
}
