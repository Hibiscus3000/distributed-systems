package ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate.repository;

import org.springframework.stereotype.Repository;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate.HashCrackState;

import java.util.UUID;

@Repository
public interface HashCrackStateRepository {

    void createNewHashCrack(UUID id);

    void updateHashCrack(HashCrackState hashCrackState);

    HashCrackState getHashCrack(UUID id);

    boolean containsId(UUID id);
}
