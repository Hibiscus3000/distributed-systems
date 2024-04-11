package ru.nsu.fit.g20203.sinyukov.rabbit.dispatch;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.nsu.fit.g20203.sinyukov.lib.IdentifiableByRequest;
import ru.nsu.fit.g20203.sinyukov.rabbit.connection.ConnectionState;

import java.util.concurrent.ExecutionException;

public class SyncRabbitDispatcher<T extends IdentifiableByRequest> extends RabbitDispatcher<T> {

    private final long connectionTimeMillis;

    public SyncRabbitDispatcher(RabbitTemplate rabbitTemplate,
                                ConnectionState connectionState,
                                Exchange exchange,
                                Binding binding,
                                String nameOfTheObjectBeingDispatched,
                                long confirmIntervalSec,
                                int maxRetryCount,
                                long connectionWaitingTimeMillis) {
        super(rabbitTemplate, connectionState, exchange, binding, nameOfTheObjectBeingDispatched, confirmIntervalSec, maxRetryCount);

        this.connectionTimeMillis = connectionWaitingTimeMillis;
    }

    @Override
    public void connectionOpened() {
        notifyAll();
    }

    public void dispatchSync(T payload) throws DispatchException {
        dispatchSync(payload, 1);
    }

    private void dispatchSync(T payload, int attemptNumber) throws DispatchException {
        logDispatchAttempt(payload.getRequestId(), attemptNumber);
        waitForConnection();
        try {
            dispatch(payload).get();
        } catch (AmqpException | ExecutionException | InterruptedException e) {
            processDispatchingException(e, payload, attemptNumber);
        }
    }

    private void waitForConnection() throws DispatchException {
        try {
            if (!connectionState.isOpen()) {
                wait(connectionTimeMillis);
            }
        } catch (InterruptedException e) {
            throw new DispatchException("Rabbit connection timeout", e);
        }

    }

    private void processDispatchingException(Throwable throwable, T payload, int attemptNumber) throws DispatchException {
        logger.warn(throwable.getMessage());
        if (maxRetryCount == attemptNumber) {
            final var ex = new DispatchException(payload.getRequestId(), throwable);
            logger.error(ex.getMessage());
            throw ex;
        } else {
            dispatchSync(payload, ++attemptNumber);
        }
    }
}
