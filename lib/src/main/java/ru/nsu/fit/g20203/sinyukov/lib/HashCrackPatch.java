package ru.nsu.fit.g20203.sinyukov.lib;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record HashCrackPatch(@NotNull UUID id, List<String> results) {

}
