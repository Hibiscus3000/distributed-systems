package ru.nsu.fit.g20203.sinyukov.manager.request.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate.repository.HashCrackStateRepository;
import ru.nsu.fit.g20203.sinyukov.manager.request.repository.HashCrackRequestRepository;

import java.util.UUID;

public class RequestIdValidator implements ConstraintValidator<KnownRequestId, UUID> {

    private final HashCrackRequestRepository hashCrackRequestRepository;
    private final HashCrackStateRepository hashCrackStateRepository;

    public RequestIdValidator(HashCrackRequestRepository hashCrackRequestRepository,
                              HashCrackStateRepository hashCrackStateRepository) {
        this.hashCrackRequestRepository = hashCrackRequestRepository;
        this.hashCrackStateRepository = hashCrackStateRepository;
    }

    @Override
    public boolean isValid(UUID value, ConstraintValidatorContext context) {
        return hashCrackRequestRepository.containsId(value) || hashCrackStateRepository.containsId(value);
    }
}
