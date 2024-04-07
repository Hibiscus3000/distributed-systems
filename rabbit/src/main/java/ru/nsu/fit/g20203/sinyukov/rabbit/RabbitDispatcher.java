package ru.nsu.fit.g20203.sinyukov.rabbit;

import org.slf4j.Logger;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import ru.nsu.fit.g20203.sinyukov.lib.Identifiable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class RabbitDispatcher<T extends Identifiable> {

    private final RabbitTemplate rabbitTemplate;

    private final String exchangeName;
    private final String bindingKey;

    protected RabbitDispatcher(RabbitTemplate rabbitTemplate, Exchange exchange, Binding binding) {
        this.rabbitTemplate = rabbitTemplate;
        exchangeName = exchange.getName();
        bindingKey = binding.getRoutingKey();
    }

    protected void dispatch(T payload) {
        dispatch(payload, 1);
    }

    private void dispatch(T payload, int attemptsCount) {
        final var correlationData = new CorrelationData();
        rabbitTemplate.convertAndSend(exchangeName, bindingKey, payload, correlationData);
        final UUID id = payload.getId();
        correlationData.getFuture().orTimeout(getConfirmIntervalSec(), TimeUnit.SECONDS)
                .thenAcceptAsync(confirm -> logConfirm(id, confirm))
                .exceptionallyAsync(t -> {
                    processConfirmTimeout(id, payload, attemptsCount);
                    return null;
                });
    }

    protected abstract long getConfirmIntervalSec();

    private void logConfirm(UUID id, CorrelationData.Confirm confirm) {
        final String objectsNamePlur = capitalizeFirstLetter(getNameOfTheObjectBeingDispatched()) + "s";
        if (confirm.isAck()) {
            getLogger().trace(id + ": " + objectsNamePlur + " dispatched");
        } else {
            getLogger().error(id + ": " + objectsNamePlur + " dispatching error: " + confirm.getReason());
        }
    }

    protected abstract Logger getLogger();

    private String capitalizeFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    protected abstract String getNameOfTheObjectBeingDispatched();

    private void processConfirmTimeout(UUID id, T payload, int attemptsCount) {
        if (getMaxRetryCount() == attemptsCount) {
            getLogger().error(String.format(id + ": Unable to dispatch %s: %s. Made %d attempt%s",
                    getNameOfTheObjectBeingDispatched(), payload, attemptsCount, getPluralOrSingular(attemptsCount)));
        } else {
            getLogger().warn(String.format(id + ": Unable to dispatch %s: %s. Made %d attempt%s. Trying one more time...",
                    getNameOfTheObjectBeingDispatched(), payload, attemptsCount, getPluralOrSingular(attemptsCount)));
            dispatch(payload, attemptsCount);
        }
    }

    protected abstract int getMaxRetryCount();

    private String getPluralOrSingular(int count) {
        if (1 != count) {
            return "s";
        }
        return "";
    }
}
