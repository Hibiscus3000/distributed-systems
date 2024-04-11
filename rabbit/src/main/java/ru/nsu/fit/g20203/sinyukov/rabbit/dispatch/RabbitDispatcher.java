package ru.nsu.fit.g20203.sinyukov.rabbit.dispatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.nsu.fit.g20203.sinyukov.lib.IdentifiableByRequest;
import ru.nsu.fit.g20203.sinyukov.rabbit.connection.ConnectionState;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static ru.nsu.fit.g20203.sinyukov.lib.StringUtil.capitalizeFirstLetter;

public abstract class RabbitDispatcher<T extends IdentifiableByRequest> {

    protected final Logger logger = LoggerFactory.getLogger(RabbitDispatcher.class);

    private final RabbitTemplate rabbitTemplate;
    protected final ConnectionState connectionState;

    private final String exchangeName;
    private final String bindingKey;

    protected final String nameOfTheObjectBeingDispatched;
    protected final long confirmIntervalSec;
    protected final int maxRetryCount;

    protected RabbitDispatcher(RabbitTemplate rabbitTemplate,
                               ConnectionState connectionState,
                               Exchange exchange,
                               Binding binding,
                               String nameOfTheObjectBeingDispatched,
                               long confirmIntervalSec,
                               int maxRetryCount) {
        this.rabbitTemplate = rabbitTemplate;
        this.connectionState = connectionState;

        exchangeName = exchange.getName();
        bindingKey = binding.getRoutingKey();

        this.nameOfTheObjectBeingDispatched = nameOfTheObjectBeingDispatched;
        this.confirmIntervalSec = confirmIntervalSec;
        this.maxRetryCount = maxRetryCount;
    }

    protected CompletableFuture<?> dispatch(T payload) throws AmqpException {
        final var correlationData = new CorrelationData();
        rabbitTemplate.convertAndSend(exchangeName, bindingKey, payload, correlationData);
        final UUID id = payload.getRequestId();
        return correlationData.getFuture().thenAcceptAsync(confirm ->
                        processConfirm(id, confirm)
                )
                .orTimeout(confirmIntervalSec, TimeUnit.SECONDS);
    }

    private void processConfirm(UUID id, CorrelationData.Confirm confirm) {
        final String nameOfTheObjectBeingDispatched = capitalizeFirstLetter(this.nameOfTheObjectBeingDispatched);
        if (confirm.isAck()) {
            logger.trace(id + ": " + nameOfTheObjectBeingDispatched + " dispatch confirmed");
        } else {
            final var ex = new ConfirmNackException(id, nameOfTheObjectBeingDispatched, confirm);
            logger.error(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    protected void logDispatchAttempt(UUID id, int attemptNumber) {
        logger.debug(id + ": Trying to dispatch " + nameOfTheObjectBeingDispatched + ". Attempt: " + attemptNumber);
    }
}
