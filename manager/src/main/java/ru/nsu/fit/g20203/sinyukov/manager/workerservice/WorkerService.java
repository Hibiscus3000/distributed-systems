package ru.nsu.fit.g20203.sinyukov.manager.workerservice;

import org.springframework.stereotype.Service;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;

import java.util.List;

@Service
public interface WorkerService {

    void dispatchTasksToWorkers(List<HashCrackTask> tasks);
}
