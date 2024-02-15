package ru.nsu.fit.g20203.sinyukov.lib;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record HashCrackTask(@NotNull UUID id,
                            @NotNull String hash,
                            @NotNull List<String> alphabet,
                            int maxLength,
                            int partNumber,
                            int partCount) {

}
