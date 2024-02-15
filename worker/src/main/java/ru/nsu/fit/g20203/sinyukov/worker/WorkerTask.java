package ru.nsu.fit.g20203.sinyukov.worker;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record WorkerTask(@NotNull List<String> alphabet, @NotNull String hash, int maxLength, long toCheck,
                         int startLength,
                         long toSkip) {

}
