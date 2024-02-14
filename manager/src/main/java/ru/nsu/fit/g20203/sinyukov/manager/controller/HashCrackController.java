package ru.nsu.fit.g20203.sinyukov.manager.controller;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrack;
import ru.nsu.fit.g20203.sinyukov.manager.HashCrackRequest;

import java.util.List;
import java.util.UUID;

@RestController
@PropertySource("classpath:application.yml")
public class HashCrackController {

    @PostMapping("${externalApiPrefix}/crack")
    public UUID postHashCrackRequest(@RequestBody HashCrackRequest request) {
        throw new NotImplementedException(); //TODO
    }

    @GetMapping("${externalApiPrefix}/status")
    public HashCrack getHashCrack(@RequestParam UUID id) {
        throw new NotImplementedException(); //TODO
    }

    @PatchMapping("${internalApiPrefix}/crack/request")
    public void patchHashCrack(@RequestBody List<String> results) {
        throw new NotImplementedException(); // TODO
    }
}
