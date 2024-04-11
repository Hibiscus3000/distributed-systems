package ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.Repository;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate.HashCrackState;

import java.util.Optional;
import java.util.UUID;

@Primary
@org.springframework.stereotype.Repository
public interface MongoHashCrackStateRepository extends HashCrackStateRepository, Repository<HashCrackState, UUID> {

    Optional<HashCrackState> findById(UUID id);

    HashCrackState save(HashCrackState hashCrackState);

    @Override
    default void createNewHashCrack(UUID id) {
        save(new HashCrackState(id));
    }

    @Override
    default void updateHashCrack(HashCrackState hashCrackState) {
        save(hashCrackState);
    }

    @Override
    default HashCrackState getHashCrack(UUID id) {
        return findById(id).get();
    }

    @Override
    default boolean containsId(UUID id) {
        return findById(id).isPresent();
    }
}
