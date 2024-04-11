package ru.nsu.fit.g20203.sinyukov.lib;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document("tasks")
public class HashCrackTask implements IdentifiableByRequest {

    private final UUID id;

    @NotNull
    private final UUID requestId;

    @NotNull
    private final String hash;

    @NotNull
    private final List<String> alphabet;

    private final int maxLength;
    private final int partNumber;
    private final int partCount;

    public HashCrackTask(@JsonProperty("id") UUID id,
                         @JsonProperty("requestId") UUID requestId,
                         @JsonProperty("hash") String hash,
                         @JsonProperty("alphabet") List<String> alphabet,
                         @JsonProperty("maxLength") int maxLength,
                         @JsonProperty("partNumber") int partNumber,
                         @JsonProperty("partCount") int partCount) {
        this.id = id;
        this.requestId = requestId;
        this.hash = hash;
        this.alphabet = alphabet;
        this.maxLength = maxLength;
        this.partNumber = partNumber;
        this.partCount = partCount;
    }
    
    @Override
    public UUID getRequestId() {
        return requestId;
    }

    public UUID getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public List<String> getAlphabet() {
        return alphabet;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public int getPartCount() {
        return partCount;
    }
}
