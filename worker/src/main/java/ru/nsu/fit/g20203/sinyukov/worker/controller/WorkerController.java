package ru.nsu.fit.g20203.sinyukov.worker.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.paukov.combinatorics3.Generator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.worker.WorkerTask;

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

    private final WebClient webClient;
    private final String patchHashCrackUrl;

    public WorkerController(@Value("${manager.baseUrl}") String managerBaseUrl,
                            @Value("${manager.patchHashCrack.url}") String patchHashCrackUrl) {
        this.webClient = WebClient.create(managerBaseUrl);
        this.patchHashCrackUrl = patchHashCrackUrl;
    }

    @PostMapping("/crack/task")
    public void postHashCrackTask(@RequestBody HashCrackTask task) {
        executorService.submit(() -> {
            final WorkerTask workerTask = createWorkerTask(task);
            final List<String> results = findResults(workerTask);
            sendResultsToManager(results, ATTEMPTS);
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


    private void sendResultsToManager(List<String> results, int attempts) {
        if (0 >= attempts) {
            return;
        }
        sendResultsToManager(results).subscribe(success -> {
            if (!success) {
                sendResultsToManager(results, attempts - 1);
            }
        });
    }

    private Mono<Boolean> sendResultsToManager(List<String> results) {
        return webClient
                .patch()
                .uri(patchHashCrackUrl)
                .bodyValue(results)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeToMono(response -> Mono.just(response.statusCode().equals(HttpStatus.OK)));
    }
}
