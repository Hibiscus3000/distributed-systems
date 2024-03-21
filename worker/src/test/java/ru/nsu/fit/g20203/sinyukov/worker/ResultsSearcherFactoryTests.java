package ru.nsu.fit.g20203.sinyukov.worker;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;
import ru.nsu.fit.g20203.sinyukov.worker.resultssearch.ResultsSearcher;
import ru.nsu.fit.g20203.sinyukov.worker.resultssearch.ResultsSearcherFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ResultsSearcherFactoryTests {

    private static final String testHash = "e2fc714c4727ee9395f324cd2e7f331f";
    private static final List<String> testAlphabet = List.of("a", "b", "c", "d");
    private static final int testMaxLength = 10;

    @ParameterizedTest
    @MethodSource("provideHashCrackTasksAndResultSearchers")
    public void givenHashCrackTask_whenCreate_returnCorrectResultsSearcher(HashCrackTask hashCrackTask, ResultsSearcher resultsSearcher) {
        Assertions.assertEquals(resultsSearcher, ResultsSearcherFactory.create(hashCrackTask));
    }

    private static Stream<Arguments> provideHashCrackTasksAndResultSearchers() {
        final long numberOfWordsTotal = WorkerUtil.totalNumberOfWords(testAlphabet.size(), testMaxLength);
        return Stream.of(
                Arguments.of(new HashCrackTask(UUID.randomUUID(), testHash, testAlphabet, testMaxLength, 0, 1),
                        new ResultsSearcher(testAlphabet, testHash, testMaxLength, numberOfWordsTotal, 1, 0)),
                Arguments.of(new HashCrackTask(UUID.randomUUID(), testHash, testAlphabet, testMaxLength, 0, 2),
                        new ResultsSearcher(testAlphabet, testHash, testMaxLength, numberOfWordsTotal / 2, 1, 0)),
                Arguments.of(new HashCrackTask(UUID.randomUUID(), testHash, testAlphabet, testMaxLength, 1, 2),
                        new ResultsSearcher(testAlphabet, testHash, testMaxLength, numberOfWordsTotal / 2, 10, 349_526))
        );
    }
}
