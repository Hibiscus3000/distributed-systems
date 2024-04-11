package ru.nsu.fit.g20203.sinyukov.rabbit.dispatch;

import org.springframework.amqp.rabbit.connection.CorrelationData;

import java.util.UUID;

public class ConfirmNackException extends Exception {

    public ConfirmNackException(UUID id, String nameOfTheObjectBeingDispatched, CorrelationData.Confirm confirm) {
        super(id + ": " + nameOfTheObjectBeingDispatched + " dispatch not confirmed: " + confirm.getReason());
    }
}
