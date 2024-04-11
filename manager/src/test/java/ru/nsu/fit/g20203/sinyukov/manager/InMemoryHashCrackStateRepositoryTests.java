package ru.nsu.fit.g20203.sinyukov.manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate.HashCrackState;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate.repository.InMemoryHashCrackStateRepository;

import java.util.UUID;

public class InMemoryHashCrackStateRepositoryTests {

    @Test
    void givenCreateCommand_whenGetHashCrack_thenReturnNewHashCrackByGivenId() {
        final var hashCrackStateRepository = new InMemoryHashCrackStateRepository();
        final var id = UUID.randomUUID();

        final HashCrackState expectedHashCrackState = new HashCrackState();

        hashCrackStateRepository.createNewHashCrack(id);

        Assertions.assertTrue(hashCrackStateRepository.containsId(id));
        Assertions.assertEquals(expectedHashCrackState, hashCrackStateRepository.getHashCrack(id));
    }
}
