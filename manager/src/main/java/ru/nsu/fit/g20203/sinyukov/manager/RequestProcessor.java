package ru.nsu.fit.g20203.sinyukov.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackrequestrepository.HashCrackRequestRepository;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstaterepository.HashCrackStateRepository;
import ru.nsu.fit.g20203.sinyukov.manager.worker.WorkerTasksCreationInfo;
import ru.nsu.fit.g20203.sinyukov.manager.worker.WorkerTasksCreator;
import ru.nsu.fit.g20203.sinyukov.manager.worker.service.WorkerService;
import ru.nsu.fit.g20203.sinyukov.manager.worker.update.WorkersUpdateRepository;

import java.util.List;
import java.util.UUID;

@Component
public class RequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(RequestProcessor.class);

    private final int numberOfWorkers;
    private final WorkerService workerService;

    private final HashCrackRequestRepository hashCrackRequestRepository;
    private final HashCrackStateRepository hashCrackStateRepository;
    private final WorkersUpdateRepository workersUpdateRepository;

    private final HashCrackTimer hashCrackTimer;

    private final List<String> alphabet;

    public RequestProcessor(@Value("${workers.count}") int numberOfWorkers,
                            WorkerService workerService,
                            HashCrackRequestRepository hashCrackRequestRepository,
                            HashCrackStateRepository hashCrackStateRepository,
                            WorkersUpdateRepository workersUpdateRepository,
                            HashCrackTimer hashCrackTimer,
                            @Value("${alphabet}") List<String> alphabet) {
        this.numberOfWorkers = numberOfWorkers;
        this.workerService = workerService;
        this.hashCrackRequestRepository = hashCrackRequestRepository;
        this.hashCrackStateRepository = hashCrackStateRepository;
        this.workersUpdateRepository = workersUpdateRepository;
        this.hashCrackTimer = hashCrackTimer;
        this.alphabet = alphabet;
    }

    public UUID process(HashCrackRequest request) {
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
}
