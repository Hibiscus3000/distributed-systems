package ru.nsu.fit.g20203.sinyukov.lib;

import java.util.List;
import java.util.UUID;

public class HashCrackTaskBuilder {

    private UUID id;
    private String hash;
    private int maxLength;
    private int partNumber;
    private int partCount;
    private List<String> alphabet;

    public static HashCrackTaskBuilder create() {
        return new HashCrackTaskBuilder();
    }

    public HashCrackTaskBuilder id(UUID id) {
        this.id = id;
        return this;
    }

    public HashCrackTaskBuilder hash(String hash) {
        this.hash = hash;
        return this;
    }

    public HashCrackTaskBuilder maxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public HashCrackTaskBuilder partNumber(int partNumber) {
        this.partNumber = partNumber;
        return this;
    }

    public HashCrackTaskBuilder partCount(int partCount) {
        this.partCount = partCount;
        return this;
    }

    public HashCrackTaskBuilder alphabet(List<String> alphabet) {
        this.alphabet = alphabet;
        return this;
    }

    public HashCrackTask build() {
        return new HashCrackTask(id, hash, alphabet, maxLength, partNumber, partCount);
    }
}
