package ru.nsu.fit.g20203.sinyukov.manager.hashcrackstaterepository;

import org.springframework.stereotype.Repository;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackState;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryHashCrackStateRepository implements HashCrackStateRepository {

    private final ConcurrentMap<UUID, HashCrackState> hashCracks = new ConcurrentHashMap<>();

    @Override
    public void createNewHashCrack(UUID id) {
        hashCracks.put(id, new HashCrackState());
    }

    @Override
    public HashCrackState getHashCrack(UUID id) {
        return hashCracks.get(id);
    }
}
