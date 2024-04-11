package ru.nsu.fit.g20203.sinyukov.lib;

import java.util.UUID;

public interface IdentifiableByRequest {

    UUID getRequestId();
}
