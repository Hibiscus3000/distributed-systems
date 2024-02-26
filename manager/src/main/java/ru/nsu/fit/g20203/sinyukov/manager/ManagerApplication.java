package ru.nsu.fit.g20203.sinyukov.manager;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.nsu.fit.g20203.sinyukov.manager.workerservice.HttpWorkerService;
import ru.nsu.fit.g20203.sinyukov.manager.workerservice.WorkerService;

@SpringBootApplication
@OpenAPIDefinition
public class ManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class, args);
    }

    @Bean
    public WorkerService workerService(@Value("${workers.count}") int workersCount,
                                       @Value("${workers.urls}") String[] workerUrls,
                                       @Value("${workers.postHashCrackTask.path}") String postHashCrackTaskPath) {
        return new HttpWorkerService(workersCount, workerUrls, postHashCrackTaskPath);
    }

}
