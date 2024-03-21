package ru.nsu.fit.g20203.sinyukov.manager.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackRequest;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackRequestRepository;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackState;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackTimer;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstaterepository.HashCrackStateRepository;
import ru.nsu.fit.g20203.sinyukov.manager.requestvalidation.KnownRequestId;
import ru.nsu.fit.g20203.sinyukov.manager.worker.WorkerTasksCreationInfo;
import ru.nsu.fit.g20203.sinyukov.manager.worker.WorkerTasksCreator;
import ru.nsu.fit.g20203.sinyukov.manager.worker.service.WorkerService;
import ru.nsu.fit.g20203.sinyukov.manager.worker.update.WorkersUpdateRepository;

import java.util.List;
import java.util.UUID;

@RestController
public class ManagerController {

    private final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    private final int numberOfWorkers;
    private final List<String> alphabet;

    private final HashCrackRequestRepository hashCrackRequestRepository;
    private final WorkerService workerService;
    private final HashCrackStateRepository hashCrackStateRepository;
    private final HashCrackTimer hashCrackTimer;
    private final WorkersUpdateRepository workersUpdateRepository;

    public ManagerController(@Value("${workers.count}") int numberOfWorkers,
                             @Value("${alphabet}") List<String> alphabet,
                             HashCrackRequestRepository hashCrackRequestRepository,
                             HashCrackStateRepository hashCrackStateRepository,
                             WorkersUpdateRepository workersUpdateRepository,
                             WorkerService workerService,
                             HashCrackTimer hashCrackTimer) {
        this.numberOfWorkers = numberOfWorkers;
        this.alphabet = alphabet;
        this.hashCrackRequestRepository = hashCrackRequestRepository;
        this.workerService = workerService;
        this.hashCrackStateRepository = hashCrackStateRepository;
        this.hashCrackTimer = hashCrackTimer;
        this.workersUpdateRepository = workersUpdateRepository;
    }

    @PostMapping("${externalApiPrefix}/crack")
    public UUID postHashCrackRequest(@RequestBody @Valid HashCrackRequest request) {
        if (!hashCrackRequestRepository.containsRequest(request)) {
            createAndSendNewTask(request);
        } else {
            logRepeatedRequest(request);
        }
        return hashCrackRequestRepository.getRequestId(request);
    }

    private void createAndSendNewTask(HashCrackRequest request) {
        final UUID id = UUID.randomUUID();
        logger.info(id + ": Received new request: " + request);

        hashCrackRequestRepository.addRequest(id, request);

        createNewHashCrack(id);

        workerService.dispatchTasksToWorkers(WorkerTasksCreator.createTasks(
                new WorkerTasksCreationInfo(id, request, numberOfWorkers, alphabet)));

        hashCrackTimer.setTimeout(hashCrackStateRepository.getHashCrack(id), id);
    }

    private void createNewHashCrack(UUID id) {
        hashCrackStateRepository.createNewHashCrack(id);
        workersUpdateRepository.create(id);
    }

    private void logRepeatedRequest(HashCrackRequest request) {
        logger.info(hashCrackRequestRepository.getRequestId(request) + ": Received repeated request: " + request);
    }

    @GetMapping("${externalApiPrefix}/status")
    public HashCrackState getHashCrack(@RequestParam @KnownRequestId UUID id) {
        final HashCrackState hashCrackState = hashCrackStateRepository.getHashCrack(id);
        logger.info(id + ": Returning hash crack: " + hashCrackState);
        return hashCrackState;
    }

    @PatchMapping("${internalApiPrefix}/crack/request")
    public void patchHashCrack(@RequestBody HashCrackPatch patch) {
        final UUID id = patch.id();
        final List<String> results = patch.results();

        updateHashCrack(id, results);
        logResults(id, results);
        if (workersUpdateRepository.areAllWorkersFinished(id) || !results.isEmpty())
            hashCrackTimer.cancelTimeout(id);
    }

    private void updateHashCrack(UUID id, List<String> results) {
        final HashCrackState hashCrackState = hashCrackStateRepository.getHashCrack(id);
        hashCrackState.addResults(results);
        workersUpdateRepository.update(id);
        if (workersUpdateRepository.areAllWorkersFinished(id) && hashCrackState.ready()) {
            logger.info(String.format("%s: All workers have finished working, found results %s", id, hashCrackState.getResults()));
        }
    }

    private void logResults(UUID id, List<String> results) {
        if (!results.isEmpty()) {
            logger.info(id + ": Worker found results: " + results);
        } else {
            logger.info(id + ": Worker found no results");
        }
    }
}
