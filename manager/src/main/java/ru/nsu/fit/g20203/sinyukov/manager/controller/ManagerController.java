package ru.nsu.fit.g20203.sinyukov.manager.controller;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTaskBuilder;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackRequest;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackState;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackTimer;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstaterepository.HashCrackStateRepository;
import ru.nsu.fit.g20203.sinyukov.manager.workerservice.WorkerService;

import java.util.*;

@RestController
@PropertySource("classpath:application.yml")
public class ManagerController {

    private final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    private final int workersCount;
    private final List<String> alphabet;

    private final WorkerService workerService;
    private final HashCrackStateRepository hashCrackStateRepository;
    private final HashCrackTimer hashCrackTimer;

    private final BiMap<UUID, HashCrackRequest> hashCrackRequests = Maps.synchronizedBiMap(HashBiMap.create());
    private final Map<UUID, Integer> hashCrackUpdateCount = new HashMap<>();

    public ManagerController(@Value("${workers.count}") int workersCount,
                             @Value("${alphabet}") List<String> alphabet,
                             HashCrackStateRepository hashCrackStateRepository,
                             WorkerService workerService,
                             HashCrackTimer hashCrackTimer) {
        this.workersCount = workersCount;
        this.alphabet = alphabet;
        this.workerService = workerService;
        this.hashCrackStateRepository = hashCrackStateRepository;
        this.hashCrackTimer = hashCrackTimer;
    }

    @PostMapping("${externalApiPrefix}/crack")
    public UUID postHashCrackRequest(@RequestBody HashCrackRequest request) {
        if (!hashCrackRequests.containsValue(request)) {
            createAndSendNewTask(request);
        } else {
            receivedRepeatedRequest(request);
        }
        return getIdForRequest(request);
    }


    private void receivedRepeatedRequest(HashCrackRequest request) {
        final UUID id = getIdForRequest(request);
        logger.info(id + ": Received repeated request: " + request);
    }

    private void createAndSendNewTask(HashCrackRequest request) {
        final UUID id = UUID.randomUUID();
        logger.info(id + ": Received new request: " + request);

        // store request
        hashCrackRequests.put(id, request);

        createNewHashCrack(id);

        // create and dispatch worker tasks
        final List<HashCrackTask> tasks = createWorkerTask(id, request);
        workerService.dispatchTasksToWorkers(tasks);

        hashCrackTimer.setTimeout(hashCrackStateRepository.getHashCrack(id), id);
    }

    private UUID getIdForRequest(HashCrackRequest request) {
        return hashCrackRequests.inverse().get(request);
    }

    private void createNewHashCrack(UUID id) {
        hashCrackStateRepository.createNewHashCrack(id);
        hashCrackUpdateCount.put(id, 0);
    }

    @GetMapping("${externalApiPrefix}/status")
    public HashCrackState getHashCrack(@RequestParam UUID id) {
        checkIfIdIsPresent(id);
        final HashCrackState hashCrackState = hashCrackStateRepository.getHashCrack(id);
        logger.info(id + ": Returning hash crack: " + hashCrackState);
        return hashCrackState;
    }

    @PatchMapping("${internalApiPrefix}/crack/request")
    public void patchHashCrack(@RequestBody HashCrackPatch patch) {
        final UUID id = patch.id();
        checkIfIdIsPresent(id);
        final List<String> results = patch.results();

        updateHashCrack(id, results);
        logResults(id, results);
        if (allWorkersFinished(id) || !results.isEmpty())
            hashCrackTimer.cancelTimeout(id);
    }

    private void updateHashCrack(UUID id, List<String> results) {
        final HashCrackState hashCrackState = hashCrackStateRepository.getHashCrack(id);
        hashCrackState.addResults(results);
        hashCrackUpdateCount.computeIfPresent(id, (uuid, prev) -> prev + 1);
        final int updateCount = hashCrackUpdateCount.get(id);
        if (allWorkersFinished(id) && hashCrackState.error()) {
            logger.info(id + ": All workers have finished working and no results were found");
        }
    }

    private boolean allWorkersFinished(UUID id) {
        return hashCrackUpdateCount.get(id) == workersCount;
    }

    private void logResults(UUID id, List<String> results) {
        if (!results.isEmpty()) {
            logger.info(id + ": Worker found results: " + results);
        } else {
            logger.info(id + ": Worker found no results");
        }
    }

    private void checkIfIdIsPresent(UUID id) {
        if (!hashCrackRequests.containsKey(id)) {
            final var ex = new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No hash crack for given UUID");
            logger.warn("No crack hash for given UUID", ex);
            throw ex;
        }
    }

    private List<HashCrackTask> createWorkerTask(UUID id, HashCrackRequest request) {
        final String hash = request.hash();
        final int maxLength = request.maxLength();
        final int partCount = workersCount;
        int partNumber = 0;

        final List<HashCrackTask> tasks = new ArrayList<>();

        for (int i = 0; i < partCount; ++i) {
            final HashCrackTask task = HashCrackTaskBuilder.create()
                    .id(id)
                    .hash(hash)
                    .maxLength(maxLength)
                    .partCount(partCount)
                    .partNumber(partNumber)
                    .alphabet(alphabet)
                    .build();
            ++partNumber;
            tasks.add(task);
        }
        logger.debug(id + ": Created tasks for workers");
        return tasks;
    }
}
