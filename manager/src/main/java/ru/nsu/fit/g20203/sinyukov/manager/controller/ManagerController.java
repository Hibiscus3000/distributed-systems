package ru.nsu.fit.g20203.sinyukov.manager.controller;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTaskBuilder;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrack;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@RestController
@PropertySource("classpath:application.yml")
public class ManagerController {

    private static final int NUMBER_OF_THREADS = 4;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(NUMBER_OF_THREADS);
    private static final long TIMEOUT_MINUTES = 20;

    private final int numberOfWorkers;
    private final String[] workerUrls;

    private final ConcurrentMap<UUID, HashCrack> hashCracks = new ConcurrentHashMap<>();
    private final BiMap<UUID, HashCrackRequest> idHashCrackRequestBiMap = Maps.synchronizedBiMap(HashBiMap.create());
    // timeout map
    private final ConcurrentMap<UUID, ScheduledFuture<?>> timeoutFutures = new ConcurrentHashMap<>();

    private static final int MAX_LENGTH = 10;
    private static final int MIN_LENGTH = 1;

    public ManagerController(@Value("${workers.urls}") String[] workerUrls) {
        this.workerUrls = workerUrls;
        numberOfWorkers = workerUrls.length;
    }

    @PostMapping("${externalApiPrefix}/crack")
    public UUID postHashCrackRequest(@RequestBody HashCrackRequest request) {
        checkMaxLength(request.maxLength());

        // request with given hash has already been received
        if (idHashCrackRequestBiMap.containsValue(request)) {
            return idHashCrackRequestBiMap.inverse().get(request);
        }

        // request with new hash
        final UUID id = UUID.randomUUID();
        idHashCrackRequestBiMap.put(id, request);
        final HashCrack hashCrack = new HashCrack();
        hashCracks.put(id, hashCrack);
        
        final Map<String, HashCrackTask> tasksMap = createWorkerTask(id, request);
        sendTasksToWorkers(tasksMap);

        // timeout
        ScheduledFuture<?> timeoutFuture = executorService.schedule(hashCrack::timeout, TIMEOUT_MINUTES, TimeUnit.MINUTES);
        timeoutFutures.put(id, timeoutFuture);
        return id;
    }

    @GetMapping("${externalApiPrefix}/status")
    public HashCrack getHashCrack(@RequestParam UUID id) {
        checkId(id);
        return hashCracks.get(id);
    }

    @PatchMapping("${internalApiPrefix}/crack/request")
    public void patchHashCrack(@RequestBody HashCrackPatch patch) {
        final UUID id = patch.id();
        checkId(id);
        final List<String> results = patch.results();

        // update hash crack
        if (!results.isEmpty()) {
            final HashCrack hashCrack = hashCracks.get(id);
            hashCrack.addResults(results);
            if (timeoutFutures.containsKey(id)) {
                timeoutFutures.get(id).cancel(false);
                timeoutFutures.remove(id);
            }
        }
    }

    private void checkId(UUID id) {
        if (!hashCracks.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No hash crack for given UUID");
        }
    }

    private Map<String, HashCrackTask> createWorkerTask(UUID id, HashCrackRequest request) {
        final String hash = request.hash();
        final int maxLength = request.maxLength();
        final int partCount = numberOfWorkers;
        int partNumber = 0;

        final Map<String, HashCrackTask> tasksMap = new HashMap<>();

        for (String workerUrl : workerUrls) {
            final HashCrackTask task = HashCrackTaskBuilder.create()
                    .id(id)
                    .hash(hash)
                    .maxLength(maxLength)
                    .partCount(partCount)
                    .partNumber(partNumber)
                    .build();
            ++partNumber;
            tasksMap.put(workerUrl, task);
        }

        return tasksMap;
    }

    private void sendTasksToWorkers(Map<String, HashCrackTask> tasksMap) {
        throw new NotImplementedException(); //TODO
    }

    private void checkMaxLength(int maxLength) {
        if (MAX_LENGTH <= maxLength || maxLength <= MIN_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("maxLength should be in [%d, %d]", MIN_LENGTH, MAX_LENGTH));
        }
    }
}
