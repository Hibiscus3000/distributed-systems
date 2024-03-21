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

public class ResultsSearcherTests {

    private static final String testHash = "e2fc714c4727ee9395f324cd2e7f331f";
    private static final List<String> testAlphabet = List.of("a", "b", "c", "d");
    private static final int testMaxLength = 4;

    @ParameterizedTest
    @MethodSource("provideHashCrackTasksAndCorrectResults")
    public void givenHashCrackTask_whenFindResults_thenReturnCorrectResults(HashCrackTask hashCrackTask, List<String> results) {
        final ResultsSearcher resultsSearcher = ResultsSearcherFactory.create(hashCrackTask);

        resultsSearcher.findResults();

        Assertions.assertEquals(results, resultsSearcher.getResults());
    }

    private static Stream<Arguments> provideHashCrackTasksAndCorrectResults() {
        return Stream.of(
                Arguments.of(new HashCrackTask(UUID.randomUUID(), testHash, testAlphabet, testMaxLength, 0, 1),
                        List.of("abcd"))
        );
    }
}
