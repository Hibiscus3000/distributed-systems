package ru.nsu.fit.g20203.sinyukov.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.*;

@Component
public class HashCrackTimer {

    private final Logger logger = LoggerFactory.getLogger(HashCrackTimer.class);

    private static final int NUMBER_OF_THREADS = 4;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(NUMBER_OF_THREADS);
    private static final long TIMEOUT_MINUTES = 20;

    private final ConcurrentMap<UUID, ScheduledFuture<?>> timeoutFutures = new ConcurrentHashMap<>();

    public void setTimeout(HashCrackState hashCrackState, UUID id) {
        ScheduledFuture<?> timeoutFuture = executorService.schedule(() -> {
            if (hashCrackState.error()) {
                logger.info(id + ": Timeout");
            } else {
                logger.debug(id + ": Timeout didn't work");
            }
            timeoutFutures.remove(id);
        }, TIMEOUT_MINUTES, TimeUnit.MINUTES);
        timeoutFutures.put(id, timeoutFuture);
    }

    public void cancelTimeout(UUID id) {
        if (timeoutFutures.containsKey(id)) {
            logger.trace(id + ": Timeout canceled");
            timeoutFutures.get(id).cancel(false);
            timeoutFutures.remove(id);
        }
    }

}
