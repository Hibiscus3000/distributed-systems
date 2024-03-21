package ru.nsu.fit.g20203.sinyukov.worker.resultssearch;

import java.util.stream.Stream;

public record FixedLengthTask(String hash, Stream<String> wordsStream, long wordsToCheck) {
}
