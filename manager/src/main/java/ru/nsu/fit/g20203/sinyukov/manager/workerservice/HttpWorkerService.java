package ru.nsu.fit.g20203.sinyukov.manager.workerservice;

import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.manager.WorkerUnavailableException;

import java.time.Duration;
import java.util.List;

public class HttpWorkerService implements WorkerService {

    private final Logger logger = LoggerFactory.getLogger(HttpWorkerService.class);

    private final WebClient webClient = WebClient.create();
    private final int workersCount;
    private final String[] workerUrls;
    private final String postHashCrackTaskPath;

    // number of attempts to post worker hash crack task
    private static final int ATTEMPTS = 3;
    private static final int INITIAL_DELAY_BETWEEN_ATTEMPTS = 2;
    private static final double JITTER = 0.5;

    public HttpWorkerService(@Value("${workers.count}") int workersCount,
                             @Value("${workers.urls}") String[] workerUrls,
                             @Value("${workers.postHashCrackTask.path}") String postHashCrackTaskPath) {
        this.workerUrls = workerUrls;
        this.workersCount = workersCount;
        this.postHashCrackTaskPath = postHashCrackTaskPath;
    }

    @Override
    public void dispatchTasksToWorkers(List<HashCrackTask> tasks) {
        logger.debug("Sending tasks to workers");
        if (tasks.size() != workersCount) {
            final var ex = new RuntimeException("Number of tasks is not equal to the number of workers");
            logger.error("", ex);
            throw ex;
        }
        for (int i = 0; i < tasks.size(); ++i) {
            final String workerUrl = workerUrls[i];
            final HashCrackTask task = tasks.get(i);
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
}
