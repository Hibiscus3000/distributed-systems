package ru.nsu.fit.g20203.sinyukov.manager.worker.service;

import org.springframework.stereotype.Service;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;

import java.util.List;

@Service
public interface WorkerService {

    void dispatchTasksToWorkers(List<HashCrackTask> tasks);
}
