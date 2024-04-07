package ru.nsu.fit.g20203.sinyukov.lib;


import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record HashCrackPatch(@NotNull UUID id, Set<String> results) implements Identifiable {

    @Override
    public UUID getId() {
        return id;
    }
}
