package ru.nsu.fit.g20203.sinyukov.manager.worker.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.manager.hashcracktask.MongoHashCrackTaskRepository;
import ru.nsu.fit.g20203.sinyukov.rabbit.connection.ConnectionState;
import ru.nsu.fit.g20203.sinyukov.rabbit.dispatch.AsyncRabbitDispatcher;
import ru.nsu.fit.g20203.sinyukov.rabbit.dispatch.Dispatchable;

import java.util.ArrayList;
import java.util.List;

@Primary
@Service
public class RabbitWorkerService implements WorkerService {

    private final Logger logger = LoggerFactory.getLogger(RabbitWorkerService.class);

    private final static long CONFIRM_INTERVAL_SEC = 15;
    private final static int MAX_RETRY_COUNT = 3;
    private final static String OBJECT_TYPE_NAME = "task";

    private final AsyncRabbitDispatcher<HashCrackTask> rabbitDispatcher;

    private final MongoHashCrackTaskRepository hashCrackTaskRepository;

    public RabbitWorkerService(MongoHashCrackTaskRepository hashCrackTaskRepository,
                               RabbitTemplate rabbitTemplate,
                               ConnectionState connectionState,
                               Exchange tasksExchange,
                               Binding tasksBinding) {
        rabbitDispatcher = new AsyncRabbitDispatcher<>(rabbitTemplate,
                connectionState,
                tasksExchange,
                tasksBinding,
                OBJECT_TYPE_NAME,
                CONFIRM_INTERVAL_SEC,
                MAX_RETRY_COUNT);

        this.hashCrackTaskRepository = hashCrackTaskRepository;
    }

    @PostConstruct
    public void dispatchAllTasksFromRepository() {
        logger.info("Dispatching all pending tasks from repository");
        dispatchTasksToWorkers(hashCrackTaskRepository.findAll());
    }

    @Override
    public void dispatchTasksToWorkers(List<HashCrackTask> tasks) {
        rabbitDispatcher.dispatchAllAsync(createDispatchablesFromTasks(tasks));
    }

    private List<Dispatchable<HashCrackTask>> createDispatchablesFromTasks(List<HashCrackTask> tasks) {
        final List<Dispatchable<HashCrackTask>> dispatchables = new ArrayList<>();
        for (var task : tasks) {
            dispatchables.add(new Dispatchable<>(task, createDispatchCallback(task)));
        }
        return dispatchables;
    }

    private Runnable createDispatchCallback(HashCrackTask task) {
        return () -> hashCrackTaskRepository.deleteById(task.getId());
    }
}
