package ru.nsu.fit.g20203.sinyukov.manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class HashCrackRequestRepositoryTests {

    private static final String exampleHash = "e2fc714c4727ee9395f324cd2e7f331f";
    private static final int exampleMaxLength = 4;

    @Test
    void givenRequest_thenReturnContainsGivenRequest() {
        final var hashCrackRequestRepository = new HashCrackRequestRepository();
        final var id = UUID.randomUUID();
        final var request = new HashCrackRequest(exampleHash, exampleMaxLength);

        hashCrackRequestRepository.addRequest(id, request);

        Assertions.assertTrue(hashCrackRequestRepository.containsId(id));
        Assertions.assertTrue(hashCrackRequestRepository.containsRequest(request));
        Assertions.assertEquals(id, hashCrackRequestRepository.getRequestId(request));
    }
}
