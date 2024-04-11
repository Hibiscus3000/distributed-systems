package ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate.repository;

import org.springframework.stereotype.Repository;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate.HashCrackState;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryHashCrackStateRepository implements HashCrackStateRepository {

    private final ConcurrentMap<UUID, HashCrackState> hashCrackStates = new ConcurrentHashMap<>();

    @Override
    public void createNewHashCrack(UUID id) {
        hashCrackStates.put(id, new HashCrackState(id));
    }

    @Override
    public void updateHashCrack(HashCrackState hashCrackState) {
        hashCrackStates.put(hashCrackState.getRequestId(), hashCrackState);
    }

    @Override
    public HashCrackState getHashCrack(UUID id) {
        return hashCrackStates.get(id);
    }

    @Override
    public boolean containsId(UUID id) {
        return hashCrackStates.containsKey(id);
    }
}
