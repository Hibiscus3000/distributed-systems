package ru.nsu.fit.g20203.sinyukov.worker.managerservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.worker.ManagerUnavailableException;

import java.time.Duration;
import java.util.UUID;

@Primary
@Service
public class HttpManagerService implements ManagerService {

    private final Logger logger = LoggerFactory.getLogger(HttpManagerService.class);

    // number of attempts to patch manager hash crack
    private static final int ATTEMPTS = 3;
    private static final int INITIAL_DELAY_BETWEEN_ATTEMPTS = 2;
    private static final double JITTER = 0.5;

    private final String webClientBaseUrl;
    private final WebClient webClient;
    private final String patchHashCrackUrl;

    public HttpManagerService(@Value("${manager.baseUrl}") String managerBaseUrl,
                              @Value("${manager.apiPrefix}") String managerApiPrefix,
                              @Value("${manager.patchHashCrack.path}") String patchHashCrackUrl) {
        webClientBaseUrl = managerBaseUrl + managerApiPrefix;
        this.webClient = WebClient.create(webClientBaseUrl);
        this.patchHashCrackUrl = patchHashCrackUrl;
    }


    @Override
    public void dispatchHashCrackPatchToManager(HashCrackPatch hashCrackPatch) {
        final UUID id = hashCrackPatch.id();
        logger.debug(String.format("%s : Sending patch to manager (%s): %s", id, webClientBaseUrl + patchHashCrackUrl,
                hashCrackPatch));
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
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                            final var ex = new ManagerUnavailableException(
                                    String.format("Unable to reach manager after %d attempts", ATTEMPTS),
                                    HttpStatus.SERVICE_UNAVAILABLE);
                            logger.debug(id + ": Worker unavailable", ex);
                            return ex;
                        }))
                .doOnError(throwable ->
                        logger.warn("Error occurred while sending patch to manager", throwable))
                .subscribe();
    }
}
