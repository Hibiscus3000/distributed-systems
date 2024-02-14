package ru.nsu.fit.g20203.sinyukov.worker.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;

@RestController
@RequestMapping("${internalApiPrefix}")
public class WorkerController {

    @PostMapping("/crack/task")
    public void postHashCrackTask(@RequestBody HashCrackTask task) {
        
    }
}
