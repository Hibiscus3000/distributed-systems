package ru.nsu.fit.g20203.sinyukov.worker;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.rabbit.dispatch.DispatchException;

@Component
public class RabbitTasksListener {

    private final Worker worker;

    public RabbitTasksListener(Worker worker) {
        this.worker = worker;
    }

    @RabbitListener(queues = "${spring.rabbitmq.tasks.queue}")
    public void receiveTasks(HashCrackTask task) throws DispatchException {
        worker.processTask(task);
    }
}
