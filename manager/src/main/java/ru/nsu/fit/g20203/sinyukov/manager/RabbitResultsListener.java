package ru.nsu.fit.g20203.sinyukov.manager;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;

@Component
public class RabbitResultsListener {

    private final PatchProcessor patchProcessor;

    public RabbitResultsListener(PatchProcessor patchProcessor) {
        this.patchProcessor = patchProcessor;
    }

    @RabbitListener(queues = "${spring.rabbitmq.results.queue}")
    public void receivePatch(HashCrackPatch patch) {
        patchProcessor.process(patch);
    }
}
