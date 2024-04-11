package ru.nsu.fit.g20203.sinyukov.worker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.rabbit.dispatch.DispatchException;
import ru.nsu.fit.g20203.sinyukov.worker.Worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("${internal-api-prefix}")
public class WorkerController {

    private final Logger logger = LoggerFactory.getLogger(WorkerController.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Worker worker;

    public WorkerController(Worker worker) {
        this.worker = worker;
    }

    @PostMapping("/crack/task")
    public void postHashCrackTask(@RequestBody HashCrackTask task) {
        executorService.submit(() -> {
            try {
                worker.processTask(task);
            } catch (DispatchException e) {
                logger.error(e.getMessage());
            }
        });
    }
}
