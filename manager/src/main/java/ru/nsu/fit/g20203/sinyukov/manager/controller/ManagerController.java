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
import ru.nsu.fit.g20203.sinyukov.manager.HashCrack;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackRequest;
import ru.nsu.fit.g20203.sinyukov.manager.worker.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.manager.worker.HashCrackTaskBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@RestController
@PropertySource("classpath:application.yml")
public class ManagerController {

    private static final int NUMBER_OF_THREADS = 4;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(NUMBER_OF_THREADS);
    private static final long TIMEOUT_MILLIS = 20 * 60 * 1000;

    private int numberOfWorkers;
    @Value("${workers.urls}")
    private String[] workerUrls;

    private final ConcurrentMap<HashCrackRequest, HashCrack> hashCracks = new ConcurrentHashMap<>();
    private final BiMap<UUID, HashCrackRequest> idHashCrackRequestBiMap = Maps.synchronizedBiMap(HashBiMap.create());

    private static final int MAX_LENGTH = 10;
    private static final int MIN_LENGTH = 1;

    public ManagerController() {
        numberOfWorkers = workerUrls.length;
    }

    @PostMapping("${externalApiPrefix}/crack")
    public UUID postHashCrackRequest(@RequestBody HashCrackRequest request) {
        checkMaxLength(request.maxLength());

        if (idHashCrackRequestBiMap.containsValue(request)) {
            return idHashCrackRequestBiMap.inverse().get(request);
        }

        final UUID id = UUID.randomUUID();
        idHashCrackRequestBiMap.put(id, request);
        hashCracks.put(request, new HashCrack());
        final Map<String, HashCrackTask> tasksMap = createWorkerTask(id, request);
        sendTasksToWorkers(tasksMap);
        return id;
    }

    @GetMapping("${externalApiPrefix}/status")
    public HashCrack getHashCrack(@RequestParam UUID id) {
        throw new NotImplementedException(); //TODO
    }

    @PatchMapping("${internalApiPrefix}/crack/request")
    public void patchHashCrack(@RequestBody List<String> results) {
        throw new NotImplementedException(); // TODO
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
