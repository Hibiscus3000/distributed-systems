package ru.nsu.fit.g20203.sinyukov.worker.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.paukov.combinatorics3.Generator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.worker.ManagerUnavailableException;
import ru.nsu.fit.g20203.sinyukov.worker.WorkerTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@RestController
@RequestMapping("${internalApiPrefix}")
public class WorkerController {

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // number of attempts to patch manager hash crack
    private static final int ATTEMPTS = 3;
    private static final int INITIAL_DELAY_BETWEEN_ATTEMPTS = 2;
    private static final double JITTER = 0.5;

    private final WebClient webClient;
    private final String patchHashCrackUrl;

    public WorkerController(@Value("${manager.baseUrl}") String managerBaseUrl,
                            @Value("${manager.patchHashCrack.path}") String patchHashCrackUrl) {
        this.webClient = WebClient.create(managerBaseUrl);
        this.patchHashCrackUrl = patchHashCrackUrl;
    }

    @PostMapping("/crack/task")
    public void postHashCrackTask(@RequestBody HashCrackTask task) throws InterruptedException {
        executorService.submit(() -> {
            final WorkerTask workerTask = createWorkerTask(task);
            final List<String> results = findResults(workerTask);
            final HashCrackPatch hashCrackPatch = new HashCrackPatch(task.id(), results);
            sendHashCrackPatchToManager(hashCrackPatch);
        });
    }

    private WorkerTask createWorkerTask(HashCrackTask task) {
        final int maxLength = task.maxLength();
        final String[] alphabet = task.alphabet();

        // total number of words to check for all workers
        long total = 0;
        for (int i = 1; i <= maxLength; ++i) {
            total += (long) Math.pow(alphabet.length, i);
        }
        // number of words to check for every worker
        final long toCheck = total / task.partCount();
        // number of words to skip for this worker
        final long toSkip = toCheck * task.partNumber();

        long skipped = 0;
        // length of the word from which the check will begin
        int l;
        for (l = 1; l <= maxLength; ++l) {
            // number of words of length l
            final long givenLengthCount = (long) Math.pow(alphabet.length, l);
            if (skipped + givenLengthCount > toSkip) {
                break;
            }
            skipped += givenLengthCount;
        }

        return new WorkerTask(alphabet, task.hash(), maxLength, toCheck, l, toSkip - skipped);
    }

    private List<String> findResults(WorkerTask task) {
        final List<String> results = new ArrayList<>();
        long wordsChecked = 0;
        for (int l = task.startLength(); l <= task.maxLength(); ++l) {
            Stream<String> wordsStream = Generator.permutation(task.alphabet())
                    .withRepetitions(l)
                    .stream()
                    .map(list -> String.join("", list));
            if (task.startLength() == l) {
                wordsStream = wordsStream.skip(task.toSkip());
            }
            final Iterator<String> wordsIterator = wordsStream.iterator();
            while (wordsIterator.hasNext()) {
                final String word = wordsIterator.next();
                if (task.toCheck() == wordsChecked) {
                    return results;
                }
                final String wordHash = DigestUtils.md5Hex(word);
                if (wordHash.equals(task.hash())) {
                    results.add(word);
                }
                ++wordsChecked;
            }
        }
        return results;
    }

    private void sendHashCrackPatchToManager(HashCrackPatch hashCrackPatch) {
        webClient.patch()
                .uri(patchHashCrackUrl)
                .bodyValue(hashCrackPatch)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ManagerUnavailableException(response.statusCode())))
                .bodyToMono(Void.class)
                .retryWhen(Retry.backoff(ATTEMPTS, Duration.ofSeconds(INITIAL_DELAY_BETWEEN_ATTEMPTS))
                        .jitter(JITTER)
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                new ManagerUnavailableException("Unable to reach worker after " + ATTEMPTS + " attempts",
                                        HttpStatus.SERVICE_UNAVAILABLE)))
                .onErrorComplete()
                .subscribe();
    }
}
