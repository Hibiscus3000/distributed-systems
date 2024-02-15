package ru.nsu.fit.g20203.sinyukov.manager.controller;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTaskBuilder;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrack;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackRequest;
import ru.nsu.fit.g20203.sinyukov.manager.WorkerUnavailableException;

import java.time.Duration;
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

    // number of attempts to post worker hash crack task
    private static final int ATTEMPTS = 3;
    private static final int INITIAL_DELAY_BETWEEN_ATTEMPTS = 2;
    private static final double JITTER = 0.5;

    private final WebClient webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(1)))).build();
    private final int numberOfWorkers;
    private final String[] workerUrls;
    private final String postHashCrackTaskPath;

    private final String[] alphabet;

    private static final int MAX_LENGTH = 10;
    private static final int MIN_LENGTH = 1;

    private final ConcurrentMap<UUID, HashCrack> hashCracks = new ConcurrentHashMap<>();
    private final BiMap<UUID, HashCrackRequest> idHashCrackRequestBiMap = Maps.synchronizedBiMap(HashBiMap.create());
    // timeout map
    private final ConcurrentMap<UUID, ScheduledFuture<?>> timeoutFutures = new ConcurrentHashMap<>();

    public ManagerController(@Value("${workers.urls}") String[] workerUrls,
                             @Value("${workers.postHashCrackTask.path}") String postHashCrackTaskPath,
                             @Value("${alphabet}") String[] alphabet) {
        this.workerUrls = workerUrls;
        numberOfWorkers = workerUrls.length;
        this.postHashCrackTaskPath = postHashCrackTaskPath;

        this.alphabet = alphabet;
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
        ScheduledFuture<?> timeoutFuture = executorService.schedule(() -> {
            hashCrack.timeout();
            timeoutFutures.remove(id);
        }, TIMEOUT_MINUTES, TimeUnit.MINUTES);
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
                    .alphabet(alphabet)
                    .build();
            ++partNumber;
            tasksMap.put(workerUrl, task);
        }

        return tasksMap;
    }

    private void sendTasksToWorkers(Map<String, HashCrackTask> tasksMap) {
        for (var task : tasksMap.entrySet()) {
            webClient.post()
                    .uri(task.getKey() + postHashCrackTaskPath)
                    .bodyValue(task.getValue())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError,
                            response -> Mono.error(new WorkerUnavailableException(response.statusCode())))
                    .bodyToMono(Void.class)
                    .retryWhen(Retry.backoff(ATTEMPTS, Duration.ofSeconds(INITIAL_DELAY_BETWEEN_ATTEMPTS))
                            .jitter(JITTER)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                    new WorkerUnavailableException("Unable to reach worker after " + ATTEMPTS + " attempts",
                                            HttpStatus.SERVICE_UNAVAILABLE)))
                    .onErrorComplete()
                    .subscribe();
        }
    }

    private void checkMaxLength(int maxLength) {
        if (MAX_LENGTH < maxLength || maxLength < MIN_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("maxLength should be in [%d, %d]", MIN_LENGTH, MAX_LENGTH));
        }
    }
}
