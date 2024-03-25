package ru.nsu.fit.g20203.sinyukov.worker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.worker.managerservice.ManagerService;
import ru.nsu.fit.g20203.sinyukov.worker.resultssearch.ResultsSearcher;
import ru.nsu.fit.g20203.sinyukov.worker.resultssearch.ResultsSearcherFactory;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("${internalApiPrefix}")
public class WorkerController {

    private final Logger logger = LoggerFactory.getLogger(WorkerController.class);
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final ManagerService managerService;

    public WorkerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @PostMapping("/crack/task")
    public void postHashCrackTask(@RequestBody HashCrackTask task) {
        executorService.submit(() -> {
            final UUID id = task.id();
            logger.info(id + ": Task received: " + task);

            findAndSendResults(id, task);
        });
    }

    private void findAndSendResults(UUID id, HashCrackTask task) {
        executorService.submit(() -> {
            final ResultsSearcher resultsSearcher = ResultsSearcherFactory.create(task);
            logger.debug(id + ": Results searcher created: " + resultsSearcher);
            final Set<String> results = findResults(id, resultsSearcher);
            sendHashCrackPatch(id, results);
        });
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
