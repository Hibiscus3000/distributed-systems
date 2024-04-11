package ru.nsu.fit.g20203.sinyukov.manager.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.manager.PatchProcessor;
import ru.nsu.fit.g20203.sinyukov.manager.RequestProcessor;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate.HashCrackState;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstate.repository.HashCrackStateRepository;
import ru.nsu.fit.g20203.sinyukov.manager.request.HashCrackRequest;
import ru.nsu.fit.g20203.sinyukov.manager.request.validation.KnownRequestId;

import java.util.UUID;

@RestController
public class ManagerController {

    private final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    private final RequestProcessor requestProcessor;
    private final PatchProcessor patchProcessor;
    
    private final HashCrackStateRepository hashCrackStateRepository;

    public ManagerController(RequestProcessor requestProcessor,
                             PatchProcessor patchProcessor,
                             HashCrackStateRepository hashCrackStateRepository) {
        this.requestProcessor = requestProcessor;
        this.patchProcessor = patchProcessor;
        this.hashCrackStateRepository = hashCrackStateRepository;
    }

    @PostMapping("${external-api-prefix}/crack")
    public UUID postHashCrackRequest(@RequestBody @Valid HashCrackRequest request) {
        return requestProcessor.process(request);
    }

    @GetMapping("${external-api-prefix}/status")
    public HashCrackState getHashCrack(@RequestParam @KnownRequestId UUID id) {
        final HashCrackState hashCrackState = hashCrackStateRepository.getHashCrack(id);
        logger.info(id + ": Returning hash crack: " + hashCrackState);
        return hashCrackState;
    }

    @PatchMapping("${internal-api-prefix}/crack/request")
    public void patchHashCrack(@RequestBody HashCrackPatch patch) {
        patchProcessor.process(patch);
    }
}
