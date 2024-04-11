package ru.nsu.fit.g20203.sinyukov.worker.manager.service;

import org.springframework.stereotype.Service;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.rabbit.dispatch.DispatchException;

@Service
public interface ManagerService {

    void dispatchHashCrackPatchToManager(HashCrackPatch hashCrackPatch) throws DispatchException;
}
