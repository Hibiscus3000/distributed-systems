package ru.nsu.fit.g20203.sinyukov.manager.request.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.Repository;
import ru.nsu.fit.g20203.sinyukov.manager.request.HashCrackRequest;
import ru.nsu.fit.g20203.sinyukov.manager.request.HashCrackRequestWithId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Primary
@org.springframework.stereotype.Repository
public interface MongoHashCrackRequestRepository extends HashCrackRequestRepository, Repository<HashCrackRequestWithId, UUID> {

    List<HashCrackRequestWithId> findByRequest(HashCrackRequest request);

    Optional<HashCrackRequestWithId> findById(UUID id);

    HashCrackRequestWithId save(HashCrackRequestWithId hashCrackRequestWithId);

    @Override
    default boolean containsRequest(HashCrackRequest request) {
        // TODO change after testing
        // return !findByRequest(request).isEmpty();
        return false;
    }

    @Override
    default boolean containsId(UUID id) {
        return findById(id).isPresent();
    }

    @Override
    default void addRequest(UUID id, HashCrackRequest request) {
        save(new HashCrackRequestWithId(id, request));
    }

    @Override
    default UUID getRequestId(HashCrackRequest request) {
        return findByRequest(request).get(0).id();
    }
}
