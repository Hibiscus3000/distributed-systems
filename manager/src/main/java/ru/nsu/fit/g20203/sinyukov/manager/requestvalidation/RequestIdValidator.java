package ru.nsu.fit.g20203.sinyukov.manager.requestvalidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackRequestRepository;

import java.util.UUID;

public class RequestIdValidator implements ConstraintValidator<KnownRequestId, UUID> {

    private final HashCrackRequestRepository hashCrackRequestRepository;

    public RequestIdValidator(HashCrackRequestRepository hashCrackRequestRepository) {
        this.hashCrackRequestRepository = hashCrackRequestRepository;
    }

    @Override
    public boolean isValid(UUID value, ConstraintValidatorContext context) {
        return hashCrackRequestRepository.containsId(value);
    }
}
