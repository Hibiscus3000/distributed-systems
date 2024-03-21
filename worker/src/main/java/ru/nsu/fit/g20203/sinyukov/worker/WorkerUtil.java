package ru.nsu.fit.g20203.sinyukov.worker;

public class WorkerUtil {

    public static long numberOfWordsOfGivenLength(int alphabetLength, int length) {
        return (long) Math.pow(alphabetLength, length);
    }

    public static long totalNumberOfWords(int alphabetLength, int maxLength) {
        return alphabetLength * ((long) Math.pow(alphabetLength, maxLength) - 1) / (alphabetLength - 1);
    }
}
