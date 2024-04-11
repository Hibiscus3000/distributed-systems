package ru.nsu.fit.g20203.sinyukov.worker.manager.service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.rabbit.connection.ConnectionState;
import ru.nsu.fit.g20203.sinyukov.rabbit.dispatch.DispatchException;
import ru.nsu.fit.g20203.sinyukov.rabbit.dispatch.SyncRabbitDispatcher;

@Primary
@Service
public class RabbitManagerService implements ManagerService {

    private final static long CONNECTION_WAITING_TIME_SEC = 300;
    private final static long CONFIRM_INTERVAL_SEC = 15;
    private final static int MAX_RETRY_COUNT = 3;
    private final static String OBJECT_TYPE_NAME = "result";

    private final SyncRabbitDispatcher<HashCrackPatch> rabbitDispatcher;

    public RabbitManagerService(RabbitTemplate rabbitTemplate,
                                ConnectionState connectionState,
                                Exchange resultsExchange,
                                Binding resultsBinding) {
        rabbitDispatcher = new SyncRabbitDispatcher<>(rabbitTemplate,
                connectionState,
                resultsExchange,
                resultsBinding,
                OBJECT_TYPE_NAME,
                CONFIRM_INTERVAL_SEC,
                MAX_RETRY_COUNT,
                CONNECTION_WAITING_TIME_SEC);
    }

    @Override
    public void dispatchHashCrackPatchToManager(HashCrackPatch hashCrackPatch) throws DispatchException {
        rabbitDispatcher.dispatchSync(hashCrackPatch);
    }
}
