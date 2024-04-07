package ru.nsu.fit.g20203.sinyukov.worker.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.rabbit.RabbitDispatcher;

@Primary
@Service
public class RabbitManagerService extends RabbitDispatcher<HashCrackPatch> implements ManagerService {

    private final Logger logger = LoggerFactory.getLogger(RabbitManagerService.class);

    private final static long CONFIRM_INTERVAL_SEC = 15;
    private final static int MAX_RETRY_COUNT = 3;
    private final static String OBJECT_TYPE_NAME = "result";

    public RabbitManagerService(RabbitTemplate rabbitTemplate,
                                Exchange resultsExchange,
                                Binding resultsBinding) {
        super(rabbitTemplate, resultsExchange, resultsBinding);
    }

    @Override
    public void dispatchHashCrackPatchToManager(HashCrackPatch hashCrackPatch) {
        dispatch(hashCrackPatch);
    }

    @Override
    protected long getConfirmIntervalSec() {
        return CONFIRM_INTERVAL_SEC;
    }

    @Override
    protected int getMaxRetryCount() {
        return MAX_RETRY_COUNT;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected String getNameOfTheObjectBeingDispatched() {
        return OBJECT_TYPE_NAME;
    }
}
