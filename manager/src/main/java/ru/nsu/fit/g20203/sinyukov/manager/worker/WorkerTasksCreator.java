package ru.nsu.fit.g20203.sinyukov.manager.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTaskBuilder;
import ru.nsu.fit.g20203.sinyukov.manager.request.HashCrackRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorkerTasksCreator {

    private static final Logger logger = LoggerFactory.getLogger(WorkerTasksCreator.class);

    public static List<HashCrackTask> createTasks(WorkerTasksCreationInfo tasksCreationInfo) {
        final HashCrackRequest request = tasksCreationInfo.request();
        final UUID id = tasksCreationInfo.id();
        final List<String> alphabet = tasksCreationInfo.alphabet();

        final String hash = request.hash();
        final int maxLength = request.maxLength();
        final int partCount = tasksCreationInfo.numberOfWorkers();
        int partNumber = 0;

        final List<HashCrackTask> tasks = new ArrayList<>();

        for (int i = 0; i < partCount; ++i) {
            final HashCrackTask task = HashCrackTaskBuilder.create()
                    .id(UUID.randomUUID())
                    .requestId(id)
                    .hash(hash)
                    .maxLength(maxLength)
                    .partCount(partCount)
                    .partNumber(partNumber)
                    .alphabet(alphabet)
                    .build();
            ++partNumber;
            tasks.add(task);
        }
        logger.debug(id + ": Created tasks for workers");
        return tasks;
    }
}
