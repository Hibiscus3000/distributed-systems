package ru.nsu.fit.g20203.sinyukov.manager.request.repository;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Repository;
import ru.nsu.fit.g20203.sinyukov.manager.request.HashCrackRequest;

import java.util.UUID;

@Repository
public class InMemoryHashCrackRequestRepository implements HashCrackRequestRepository {

    private final BiMap<UUID, HashCrackRequest> hashCrackRequests = Maps.synchronizedBiMap(HashBiMap.create());

    @Override
    public boolean containsRequest(HashCrackRequest request) {
        return hashCrackRequests.containsValue(request);
    }

    @Override
    public boolean containsId(UUID id) {
        return hashCrackRequests.containsKey(id);
    }

    @Override
    public void addRequest(UUID id, HashCrackRequest request) {
        hashCrackRequests.put(id, request);
    }

    @Override
    public UUID getRequestId(HashCrackRequest request) {
        return hashCrackRequests.inverse().get(request);
    }
}
