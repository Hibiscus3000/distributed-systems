package ru.nsu.fit.g20203.sinyukov.manager.requestvalidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackrequestrepository.HashCrackRequestRepository;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstaterepository.HashCrackStateRepository;

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
