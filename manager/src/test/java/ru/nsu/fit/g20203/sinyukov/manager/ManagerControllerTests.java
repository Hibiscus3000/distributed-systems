package ru.nsu.fit.g20203.sinyukov.manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.nsu.fit.g20203.sinyukov.lib.HashCrackPatch;
import ru.nsu.fit.g20203.sinyukov.manager.controller.ManagerController;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackrequestrepository.HashCrackRequestRepository;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstaterepository.HashCrackStateRepository;
import ru.nsu.fit.g20203.sinyukov.manager.worker.service.WorkerService;
import ru.nsu.fit.g20203.sinyukov.manager.worker.update.WorkersUpdateRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@WebMvcTest(ManagerController.class)
class ManagerControllerTests {

    @MockBean
    private WorkerService service;
    @MockBean
    private HashCrackRequestRepository hashCrackRequestRepository;
    @MockBean
    private HashCrackStateRepository hashCrackStateRepository;
    @MockBean
    private HashCrackTimer hashCrackTimer;
    @MockBean
    private WorkersUpdateRepository workersUpdateRepository;

    private final WebTestClient client;

    private static final String testHash = "e2fc714c4727ee9395f324cd2e7f331f";
    private static final String invalidHash = "123";

    private static final List<String> testResults = List.of("abcd");
    
    public ManagerControllerTests(@Autowired WebTestClient client) {
        this.client = client;
    }

    @Test
    public void givenValidRequest_whenPostHashCrackRequest_thenReturnOk() {
        final HashCrackRequest request = new HashCrackRequest(testHash, 4);
        client.post()
                .uri("/api/hash/crack")
                .body(Mono.just(request), HashCrackRequest.class)
                .exchange()
                .expectStatus().isOk();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRequests")
    public void givenInvalidRequest_whenPostHashCrackRequest_thenReturnBadRequest(TestHashCrackRequest request) {
        client.post()
                .uri("/api/hash/crack")
                .body(Mono.just(request), TestHashCrackRequest.class)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private static Stream<Arguments> provideInvalidRequests() {
        return Stream.of(
                Arguments.of(new TestHashCrackRequest(invalidHash, 4)),
                Arguments.of(new TestHashCrackRequest(testHash, 40)),
                Arguments.of(new TestHashCrackRequest(testHash, -10))
        );
    }

    @Test
    public void givenHashCrackState_whenGetHashCrack_thenReturnHashCrackState() {
        final UUID id = UUID.randomUUID();
        final HashCrackState hashCrackState = new HashCrackState();
        hashCrackState.addResults(testResults);
        addHashCrackStateInRepositoryMock(id, hashCrackState);

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/hash/status")
                        .queryParam("id", id)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(HashCrackState.class)
                .consumeWith(exchangeResult -> {
                    final HashCrackState actualHashCrackState = exchangeResult.getResponseBody();
                    Assertions.assertLinesMatch(testResults, actualHashCrackState.getResults());
                    Assertions.assertEquals(HashCrackState.HashCrackStatus.READY, actualHashCrackState.getStatus());
                });
    }

    private void addHashCrackStateInRepositoryMock(UUID id, HashCrackState hashCrackState) {
        Mockito.when(hashCrackStateRepository.getHashCrack(id)).thenReturn(hashCrackState);
        Mockito.when(hashCrackStateRepository.containsId(id)).thenReturn(true);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidIds")
    public void givenInvalidId_whenGetHashCrack_thenReturnBadRequest(String id) {
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/hash/status")
                        .queryParam("id", id)
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    private static Stream<Arguments> provideInvalidIds() {
        return Stream.of(
                Arguments.of("aaaAAA"),
                Arguments.of(UUID.randomUUID().toString())
        );
    }

    @Test
    public void givenHashCrackState_whenPatchHashCrack_thenUpdateHashCrackState() {
        final UUID id = UUID.randomUUID();
        final HashCrackState hashCrackState = new HashCrackState();
        addHashCrackStateInRepositoryMock(id, hashCrackState);
        Mockito.when(workersUpdateRepository.areAllWorkersFinished(id)).thenReturn(true);

        client.patch()
                .uri("/internal/api/manager/hash/crack/request")
                .body(Mono.just(new HashCrackPatch(id, testResults)), HashCrackPatch.class)
                .exchange()
                .expectStatus().isOk();

        Assertions.assertLinesMatch(testResults, hashCrackState.getResults());
        Assertions.assertEquals(HashCrackState.HashCrackStatus.READY, hashCrackState.getStatus());
    }
}
