package ru.nsu.fit.g20203.sinyukov.worker.resultssearch;

import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.worker.WorkerUtil;

import java.util.List;

public class ResultsSearcherFactory {

    public static ResultsSearcher create(HashCrackTask task) {
        final int maxLength = task.getMaxLength();
        final List<String> alphabet = task.getAlphabet();

        // total number of words to check for all workers
        long total = WorkerUtil.totalNumberOfWords(alphabet.size(), maxLength);
        // number of words to check for every worker
        final long toCheck = total / task.getPartCount();
        // number of words to skip for this worker
        final long toSkip = toCheck * task.getPartNumber();

        long skipped = 0;
        // length of the word from which the check will begin
        int l;
        for (l = 1; l <= maxLength; ++l) {
            // number of words of length l
            final long givenLengthCount = WorkerUtil.numberOfWordsOfGivenLength(alphabet.size(), l);
            if (skipped + givenLengthCount > toSkip) {
                break;
            }
            skipped += givenLengthCount;
        }

        return new ResultsSearcher(alphabet, task.getHash(), maxLength, toCheck, l, toSkip - skipped);
    }
}
