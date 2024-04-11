package ru.nsu.fit.g20203.sinyukov.manager.hashcracktask;

import org.springframework.data.repository.Repository;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackTask;

import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Repository
public interface MongoHashCrackTaskRepository extends Repository<HashCrackTask, UUID> {

    List<HashCrackTask> findAll();

    void deleteById(UUID id);

    Iterable<HashCrackTask> saveAll(Iterable<HashCrackTask> tasks);
}
