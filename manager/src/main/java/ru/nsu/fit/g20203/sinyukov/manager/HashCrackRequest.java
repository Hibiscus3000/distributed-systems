package ru.nsu.fit.g20203.sinyukov.manager;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record HashCrackRequest(@NotNull String hash,
                               @Min(value = MIN_LENGTH, message = invalidLengthMessage)
                               @Max(value = MAX_LENGTH, message = invalidLengthMessage)
                               int maxLength) {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 10;
    private static final String invalidLengthMessage = "maxLength should be in [" + MIN_LENGTH + ", " + MAX_LENGTH + "]";
}
