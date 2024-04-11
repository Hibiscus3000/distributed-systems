package ru.nsu.fit.g20203.sinyukov.manager.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import ru.nsu.fit.g20203.sinyukov.lib.IdentifiableByRequest;

import java.util.UUID;

@Document("requests")
public record HashCrackRequestWithId(@MongoId @NotNull UUID id,
                                     HashCrackRequest request) implements IdentifiableByRequest {

    @Override
    public UUID getRequestId() {
        return id;
    }
}
