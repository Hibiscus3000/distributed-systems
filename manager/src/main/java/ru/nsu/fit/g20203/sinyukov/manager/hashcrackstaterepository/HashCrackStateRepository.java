package ru.nsu.fit.g20203.sinyukov.manager.hashcrackstaterepository;

import org.springframework.stereotype.Repository;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackState;

import java.util.UUID;

@Repository
public interface HashCrackStateRepository {

    void createNewHashCrack(UUID id);

    HashCrackState getHashCrack(UUID id);

    boolean containsId(UUID id);
}
