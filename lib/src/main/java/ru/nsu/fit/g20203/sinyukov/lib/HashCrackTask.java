package ru.nsu.fit.g20203.sinyukov.lib;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document("tasks")
public record HashCrackTask(UUID id,
                            UUID requestId,
                            String hash,
                            List<String> alphabet,
                            int maxLength,
                            int partNumber,
                            int partCount) implements IdentifiableByRequest {

    @Override
    public UUID getRequestId() {
        return requestId;
    }
}
