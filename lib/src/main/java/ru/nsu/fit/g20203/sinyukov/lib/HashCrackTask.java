package ru.nsu.fit.g20203.sinyukov.lib;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record HashCrackTask(@NotNull UUID id,
                            @NotNull String hash,
                            int maxLength,
                            int partNumber,
                            int partCount) {


}
