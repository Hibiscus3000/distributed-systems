package ru.nsu.fit.g20203.sinyukov.manager.hashcrackstaterepository;

import ru.nsu.fit.g20203.sinyukov.manager.HashCrackState;

import java.util.UUID;

public interface HashCrackStateRepository {

    void createNewHashCrack(UUID id);

    HashCrackState getHashCrack(UUID id);

    boolean containsId(UUID id);
}
