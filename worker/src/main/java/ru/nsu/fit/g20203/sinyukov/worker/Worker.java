package ru.nsu.fit.g20203.sinyukov.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.worker.manager.service.ManagerService;
import ru.nsu.fit.g20203.sinyukov.worker.resultssearch.ResultsSearcher;
import ru.nsu.fit.g20203.sinyukov.worker.resultssearch.ResultsSearcherFactory;

import java.util.Set;
import java.util.UUID;

@Component
public class Worker {

    private final Logger logger = LoggerFactory.getLogger(Worker.class);

    private final ManagerService managerService;

    public Worker(ManagerService managerService) {
        this.managerService = managerService;
    }

    public void processTask(HashCrackTask task) {
        final UUID id = task.id();
        logger.info(id + ": Task received: " + task);
        findAndSendResults(id, task);
    }

    private void findAndSendResults(UUID id, HashCrackTask task) {
        final ResultsSearcher resultsSearcher = ResultsSearcherFactory.create(task);
        logger.debug(id + ": Results searcher created: " + resultsSearcher);
        final Set<String> results = findResults(id, resultsSearcher);
        sendHashCrackPatch(id, results);
    }

    private Set<String> findResults(UUID id, ResultsSearcher resultsSearcher) {
        resultsSearcher.findResults();
        final Set<String> results = resultsSearcher.getResults();
        logResults(id, results);
        return results;
    }

    private void logResults(UUID id, Set<String> results) {
        if (results.isEmpty()) {
            logger.info(id + ": Worker found no results");
        } else {
            logger.info(id + ": Worker found results: " + results);
        }
    }

    private void sendHashCrackPatch(UUID id, Set<String> results) {
        final HashCrackPatch hashCrackPatch = new HashCrackPatch(id, results);
        managerService.dispatchHashCrackPatchToManager(hashCrackPatch);
    }
}
