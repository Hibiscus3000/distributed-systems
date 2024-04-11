package ru.nsu.fit.g20203.sinyukov.manager.worker;

import ru.nsu.fit.g20203.sinyukov.manager.request.HashCrackRequest;

import java.util.List;
import java.util.UUID;

public record WorkerTasksCreationInfo(UUID id, HashCrackRequest request, int numberOfWorkers, List<String> alphabet) {
}
