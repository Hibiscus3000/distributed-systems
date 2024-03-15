package ru.nsu.fit.g20203.sinyukov.manager;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class HashCrackRequestRepository {

    private final BiMap<UUID, HashCrackRequest> hashCrackRequests = Maps.synchronizedBiMap(HashBiMap.create());

    public boolean containsRequest(HashCrackRequest request) {
        return hashCrackRequests.containsValue(request);
    }

    public boolean containsId(UUID id) {
        return hashCrackRequests.containsKey(id);
    }

    public void addRequest(UUID id, HashCrackRequest request) {
        hashCrackRequests.put(id, request);
    }

    public UUID getRequestId(HashCrackRequest request) {
        return hashCrackRequests.inverse().get(request);
    }
}
