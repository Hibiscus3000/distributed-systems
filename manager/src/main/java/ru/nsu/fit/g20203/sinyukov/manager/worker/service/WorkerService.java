package ru.nsu.fit.g20203.sinyukov.manager.worker.service;

import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;

import java.util.List;

public interface WorkerService {

    void dispatchTasksToWorkers(List<HashCrackTask> tasks);
}
