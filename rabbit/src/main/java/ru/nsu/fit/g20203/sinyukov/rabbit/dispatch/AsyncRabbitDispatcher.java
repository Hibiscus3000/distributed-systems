package ru.nsu.fit.g20203.sinyukov.rabbit.dispatch;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.nsu.fit.g20203.sinyukov.lib.IdentifiableByRequest;
import ru.nsu.fit.g20203.sinyukov.rabbit.connection.ConnectionState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static ru.nsu.fit.g20203.sinyukov.lib.StringUtil.capitalizeFirstLetter;
import static ru.nsu.fit.g20203.sinyukov.lib.StringUtil.getPluralOrSingular;

public class AsyncRabbitDispatcher<T extends IdentifiableByRequest> extends RabbitDispatcher<T> {

    private final List<Dispatchable<T>> toDispatch = new ArrayList<>();

    public AsyncRabbitDispatcher(RabbitTemplate rabbitTemplate,
                                 ConnectionState connectionState,
                                 Exchange exchange,
                                 Binding binding,
                                 String nameOfTheObjectBeingDispatched,
                                 long confirmIntervalSec,
                                 int maxRetryCount) {
        super(rabbitTemplate, connectionState, exchange, binding, nameOfTheObjectBeingDispatched, confirmIntervalSec, maxRetryCount);
    }

    @Override
    public void connectionOpened() {
        logger.info("Connection restored. Dispatching all pending tasks");
        dispatchAllAsync(toDispatch);
    }

    public void dispatchAllAsync(Collection<Dispatchable<T>> toDispatch) {
        for (var dispatchable : toDispatch) {
            dispatchAsync(dispatchable);
        }
    }

    public void dispatchAsync(Dispatchable<T> dispatchable) {
        dispatchAsync(dispatchable, 1);
    }

    private void dispatchAsync(Dispatchable<T> dispatchable, int attemptNumber) {
        final T payload = dispatchable.getPayload();
        logDispatchAttempt(payload.getRequestId(), attemptNumber);
        if (!connectionState.isOpen()) {
            addToDispatchLater(dispatchable);
            return;
        }
        try {
            dispatch(payload)
                    .whenCompleteAsync((nothing, throwable) -> {
                        if (null != throwable) {
                            processDispatchingException(dispatchable, attemptNumber, throwable);
                        } else {
                            dispatchable.dispatchedSuccessfully();
                        }
                    });
        } catch (AmqpException e) {
            addToDispatchLater(dispatchable);
        }
    }

    private void addToDispatchLater(Dispatchable<T> dispatchable) {
        toDispatch.add(dispatchable);
        final String nameOfTheObjectBeingDispatcher = capitalizeFirstLetter(nameOfTheObjectBeingDispatched);
        logger.info(String.format("%s: %s will be dispatcher later due to connection unavailability",
                dispatchable.getPayload().getRequestId(),
                nameOfTheObjectBeingDispatched));
    }

    private void processDispatchingException(Dispatchable<T> dispatchable, int attemptCount, Throwable t) {
        final T payload = dispatchable.getPayload();
        final UUID id = payload.getRequestId();
        if (maxRetryCount == attemptCount) {
            logger.error(String.format(id + ": Unable to dispatch %s: %s. Cause: %s. Made %d attempt%s",
                    nameOfTheObjectBeingDispatched, payload, t.getMessage(), attemptCount, getPluralOrSingular(attemptCount)));
        } else {
            logger.warn(String.format(id + ": Unable to dispatch %s: %s. Cause: %s. Made %d attempt%s. Trying one more time...",
                    nameOfTheObjectBeingDispatched, payload, t.getMessage(), attemptCount, getPluralOrSingular(attemptCount)));
            dispatchAsync(dispatchable, ++attemptCount);
        }
    }
}