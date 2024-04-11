package ru.nsu.fit.g20203.sinyukov.manager.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HashCrackRequest(
        @Size(min = HASH_SIZE, max = HASH_SIZE, message = invalidHashSizeMessage)
        @NotNull String hash,
        @Min(value = MIN_LENGTH, message = invalidLengthMessage)
        @Max(value = MAX_LENGTH, message = invalidLengthMessage)
        int maxLength) {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 10;
    private static final String invalidLengthMessage = "maxLength should be in [" + MIN_LENGTH + ", " + MAX_LENGTH + "]";

    private static final int HASH_SIZE = 32;
    private static final String invalidHashSizeMessage = "hash size must be equal to " + HASH_SIZE;
}
