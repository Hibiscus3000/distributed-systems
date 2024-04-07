package ru.nsu.fit.g20203.sinyukov.manager.worker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.rabbit.RabbitDispatcher;

import java.util.List;

@Primary
@Service
public class RabbitWorkerService extends RabbitDispatcher<HashCrackTask> implements WorkerService {

    private final Logger logger = LoggerFactory.getLogger(RabbitWorkerService.class);

    private final static long CONFIRM_INTERVAL_SEC = 15;
    private final static int MAX_RETRY_COUNT = 3;
    private final static String OBJECT_TYPE_NAME = "task";

    public RabbitWorkerService(RabbitTemplate rabbitTemplate,
                               Exchange tasksExchange,
                               Binding tasksBinding) {
        super(rabbitTemplate, tasksExchange, tasksBinding);
    }

    @Override
    public void dispatchTasksToWorkers(List<HashCrackTask> tasks) {
        for (var task : tasks) {
            dispatch(task);
        }
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
