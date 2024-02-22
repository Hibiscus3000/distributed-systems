package ru.nsu.fit.g20203.sinyukov.manager.controller;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(ManagerController.class);

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

    private final List<String> alphabet;

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 10;

    private final ConcurrentMap<UUID, HashCrack> hashCracks = new ConcurrentHashMap<>();
    private final BiMap<UUID, HashCrackRequest> idHashCrackRequestBiMap = Maps.synchronizedBiMap(HashBiMap.create());
    // timeout map
    private final ConcurrentMap<UUID, ScheduledFuture<?>> timeoutFutures = new ConcurrentHashMap<>();

    public ManagerController(@Value("${workers.urls}") String[] workerUrls,
                             @Value("${workers.postHashCrackTask.path}") String postHashCrackTaskPath,
                             @Value("${alphabet}") List<String> alphabet) {
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
            final UUID id = idHashCrackRequestBiMap.inverse().get(request);
            logger.info(id + ": Received repeated request: " + request);
            return id;
        }

        // request with new hash
        final UUID id = UUID.randomUUID();
        logger.info(id + ": Received new request: " + request);
        idHashCrackRequestBiMap.put(id, request);
        final HashCrack hashCrack = new HashCrack();
        hashCracks.put(id, hashCrack);

        final Map<String, HashCrackTask> tasksMap = createWorkerTask(id, request);
        sendTasksToWorkers(tasksMap);

        // timeout
        ScheduledFuture<?> timeoutFuture = executorService.schedule(() -> {
            if (hashCrack.timeout()) {
                logger.info(id + ": timeout");
            }
            timeoutFutures.remove(id);
        }, TIMEOUT_MINUTES, TimeUnit.MINUTES);
        timeoutFutures.put(id, timeoutFuture);
        return id;
    }

    @GetMapping("${externalApiPrefix}/status")
    public HashCrack getHashCrack(@RequestParam UUID id) {
        checkId(id);
        final HashCrack hashCrack = hashCracks.get(id);
        logger.info(id + ": Returning hash crack: " + hashCrack);
        return hashCrack;
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
            logger.info(id + ": Worker found results: " + results);
        } else {
            logger.info(id + ": Worker found no results");
        }
    }

    private void checkId(UUID id) {
        if (!hashCracks.containsKey(id)) {
            final var ex = new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No hash crack for given UUID");
            logger.warn("No crack hash for given UUID", ex);
            throw ex;
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
        logger.debug(id + ": Created tasks for workers");
        return tasksMap;
    }

    private void sendTasksToWorkers(Map<String, HashCrackTask> tasksMap) {
        logger.debug("Sending tasks to workers");
        for (var taskEntry : tasksMap.entrySet()) {
            final String workerUrl = taskEntry.getKey();
            final HashCrackTask task = taskEntry.getValue();
            logger.trace(String.format("Sending task to worker (%s): %s", workerUrl, task));
            webClient.post()
                    .uri(workerUrl + postHashCrackTaskPath)
                    .bodyValue(task)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError,
                            response -> Mono.error(new WorkerUnavailableException(response.statusCode())))
                    .bodyToMono(Void.class)
                    .retryWhen(Retry.backoff(ATTEMPTS, Duration.ofSeconds(INITIAL_DELAY_BETWEEN_ATTEMPTS))
                            .jitter(JITTER)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                final var ex = new WorkerUnavailableException(
                                        String.format("Unable to reach worker (%s) after %d attempts", workerUrl, ATTEMPTS),
                                        HttpStatus.SERVICE_UNAVAILABLE);
                                logger.debug(task.id() + ": Worker unavailable", ex);
                                return ex;
                            }))
                    .doOnError(throwable ->
                            logger.warn(String.format("Error occurred while sending task to worker (%s)", workerUrl), throwable))
                    .subscribe();
        }
    }

    private void checkMaxLength(int maxLength) {
        if (MAX_LENGTH < maxLength || maxLength < MIN_LENGTH) {
            final var ex = new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("maxLength should be in [%d, %d]", MIN_LENGTH, MAX_LENGTH));
            logger.warn("Request declined", ex);
            throw ex;
        }
    }
}
