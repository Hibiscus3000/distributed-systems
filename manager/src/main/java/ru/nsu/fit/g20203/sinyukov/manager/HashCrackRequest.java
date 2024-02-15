package ru.nsu.fit.g20203.sinyukov.manager;

import jakarta.validation.constraints.NotNull;

public record HashCrackRequest(@NotNull String hash, int maxLength) {


}
