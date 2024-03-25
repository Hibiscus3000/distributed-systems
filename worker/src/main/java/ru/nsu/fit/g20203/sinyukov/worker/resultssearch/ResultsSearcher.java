package ru.nsu.fit.g20203.sinyukov.worker.resultssearch;

import org.apache.commons.codec.digest.DigestUtils;
import org.paukov.combinatorics3.Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

import static ru.nsu.fit.g20203.sinyukov.worker.WorkerUtil.numberOfWordsOfGivenLength;

public class ResultsSearcher {

    private final Logger logger = LoggerFactory.getLogger(ResultsSearcher.class);

    private final List<String> alphabet;
    private final String hash;
    private final int maxLength;
    private final long wordsToCheckTotal;
    private final int startLength;
    private final long wordsToSkip;

    private final Set<String> results = new HashSet<>();
    private long wordsCheckedTotal = 0;
    private boolean used = false;

    public ResultsSearcher(List<String> alphabet, String hash, int maxLength, long wordsToCheckTotal, int startLength, long wordsToSkip) {
        this.alphabet = alphabet;
        this.hash = hash;
        this.maxLength = maxLength;
        this.wordsToCheckTotal = wordsToCheckTotal;
        this.startLength = startLength;
        this.wordsToSkip = wordsToSkip;
    }

    public void findResults() {
        checkAndSetUsed();
        logger.debug("Worker starts searching");
        for (int l = startLength; l <= maxLength && wordsCheckedTotal != wordsToCheckTotal; ++l) {
            findResultsForGivenLength(l);
        }
    }

    private void checkAndSetUsed() {
        final boolean prevUsed = used;
        used = true;
        if (prevUsed) {
            throw new RuntimeException("Results searcher reuse detected");
        }
    }

    private void findResultsForGivenLength(int length) {
        logger.trace("Worker started checking words of length " + length);
        final Stream<String> wordsStream = createWordsStream(length);
        final long wordsOfGivenLengthToCheck =
                Math.min(numberOfWordsOfGivenLength(alphabet.size(), length),
                        wordsToCheckTotal - wordsCheckedTotal);
        final var fixedLengthTask = new FixedLengthTask(hash, wordsStream, wordsOfGivenLengthToCheck);
        wordsCheckedTotal += wordsOfGivenLengthToCheck;
        checkWordsOfGivenLength(fixedLengthTask);
    }

    private Stream<String> createWordsStream(int wordLength) {
        Stream<String> wordsStream = Generator.permutation(alphabet)
                .withRepetitions(wordLength)
                .stream()
                .map(list -> String.join("", list));
        if (startLength == wordLength) {
            wordsStream = wordsStream.skip(wordsToSkip);
            logger.trace("Worker skipped " + wordsToSkip + " first elements");
        }
        return wordsStream;
    }

    private void checkWordsOfGivenLength(FixedLengthTask task) {
        long wordsChecked = 0;
        final long wordsToCheck = task.wordsToCheck();
        final Iterator<String> wordsIterator = task.wordsStream().iterator();
        while (wordsIterator.hasNext()) {
            final String word = wordsIterator.next();
            if (wordsToCheck == wordsChecked) {
                break;
            }
            checkAndAddToResults(results, task.hash(), word);
            ++wordsChecked;
        }
    }

    private void checkAndAddToResults(Set<String> results, String hash, String word) {
        final String wordHash = DigestUtils.md5Hex(word);
        if (wordHash.equals(hash)) {
            logger.debug("Worker found result: " + word);
            results.add(word);
        }
    }

    public Set<String> getResults() {
        return results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultsSearcher that = (ResultsSearcher) o;
        return maxLength == that.maxLength && wordsToCheckTotal == that.wordsToCheckTotal && startLength == that.startLength && wordsToSkip == that.wordsToSkip && Objects.equals(alphabet, that.alphabet) && Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alphabet, hash, maxLength, wordsToCheckTotal, startLength, wordsToSkip);
    }

    @Override
    public String toString() {
        return "alphabet: " + alphabet +
                ", hash: '" + hash + '\'' +
                ", maxLength: " + maxLength +
                ", startLength: " + startLength +
                ", wordsToSkip: " + wordsToSkip;
    }
}

