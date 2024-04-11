package ru.nsu.fit.g20203.sinyukov.manager.request.repository;

import org.springframework.stereotype.Repository;
import ru.nsu.fit.g20203.sinyukov.manager.request.HashCrackRequest;

import java.util.UUID;

@Repository
public interface HashCrackRequestRepository {

    boolean containsRequest(HashCrackRequest request);

    boolean containsId(UUID id);

    void addRequest(UUID id, HashCrackRequest request);

    UUID getRequestId(HashCrackRequest request);
}
