package ru.nsu.fit.g20203.sinyukov.worker;

import jakarta.validation.constraints.NotNull;

public record WorkerTask(@NotNull String[] alphabet, @NotNull String hash, int maxLength, long toCheck, int startLength,
                         long toSkip) {

}
