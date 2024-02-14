package ru.nsu.fit.g20203.sinyukov.manager.worker;

import java.util.UUID;

public record HashCrackTask(UUID id, String hash, int maxLength, int partNumber, int partCount) {


}
