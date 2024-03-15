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
import ru.nsu.fit.g20203.sinyukov.manager.controller.ManagerController;
import ru.nsu.fit.g20203.sinyukov.manager.hashcrackstaterepository.HashCrackStateRepository;
import ru.nsu.fit.g20203.sinyukov.manager.worker.service.WorkerService;

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

    private final WebTestClient client;

    private static final String exampleHash = "e2fc714c4727ee9395f324cd2e7f331f";
    private static final String invalidHash = "123";

    public ManagerControllerTests(@Autowired WebTestClient client) {
        this.client = client;
    }

    @Test
    public void givenValidRequest_whenPostHashCrackRequest_thenReturnOk() {
        final HashCrackRequest request = new HashCrackRequest(exampleHash, 4);
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
                Arguments.of(new TestHashCrackRequest(exampleHash, 40)),
                Arguments.of(new TestHashCrackRequest(exampleHash, -10))
        );
    }

    @Test
    public void givenHashCrackState_whenGetHashCrack_thenReturnHashCrackState() {
        final UUID id = UUID.randomUUID();
        final HashCrackState hashCrackState = new HashCrackState();
        final List<String> results = List.of("abcd");
        hashCrackState.addResults(results);
        Mockito.when(hashCrackStateRepository.getHashCrack(id)).thenReturn(hashCrackState);
        Mockito.when(hashCrackStateRepository.containsId(id)).thenReturn(true);
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
                    Assertions.assertLinesMatch(results, actualHashCrackState.getResults());
                    Assertions.assertEquals(HashCrackState.HashCrackStatus.READY, actualHashCrackState.getStatus());
                });
    }

    @ParameterizedTest
    @MethodSource("provideInvalidIds")
    public void givenInvalidId_whenGetHashCrack_thenReturnBadRequest(String id) {
        client.get().uri(uriBuilder -> uriBuilder
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
}
