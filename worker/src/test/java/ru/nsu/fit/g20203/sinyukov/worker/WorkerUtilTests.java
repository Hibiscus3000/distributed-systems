package ru.nsu.fit.g20203.sinyukov.worker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class WorkerUtilTests {


    @ParameterizedTest
    @MethodSource("provideAlphabetLengthsWordLengthsAndCorrectNumberOfWordsForGivenLength")
    public void givenAlphabetLengthAndWordLength_whenNumberOfWordsOfGivenLength_thenReturnCorrectNumberOfWordsOfGivenLength(
            int alphabetLength, int wordLength, long correctNumberOfWords) {
        Assertions.assertEquals(correctNumberOfWords, WorkerUtil.numberOfWordsOfGivenLength(alphabetLength, wordLength));
    }

    private static Stream<Arguments> provideAlphabetLengthsWordLengthsAndCorrectNumberOfWordsForGivenLength() {
        return Stream.of(
                Arguments.of(10, 5, 100_000L),
                Arguments.of(36, 10, 3_656_158_440_062_976L),
                Arguments.of(4, 3, 64L),
                Arguments.of(7, 5, 16_807L)
        );
    }

    @ParameterizedTest
    @MethodSource("provideAlphabetLengthsMaxWordLengthsAndCorrectTotalNumberOfWords")
    public void givenAlphabetLengthAndWordLength_whenTotalNumberOfWords_thenReturnCorrectTotalNumberOfWords(
            int alphabetLength, int maxLength, long correctNumberOfWords) {
        Assertions.assertEquals(correctNumberOfWords, WorkerUtil.totalNumberOfWords(alphabetLength, maxLength));
    }

    private static Stream<Arguments> provideAlphabetLengthsMaxWordLengthsAndCorrectTotalNumberOfWords() {
        return Stream.of(
                Arguments.of(10, 5, 111_110L),
                Arguments.of(36, 10, 3_760_620_109_779_060L),
                Arguments.of(4, 3, 84L),
                Arguments.of(7, 5, 19_607L)
        );
    }


}
