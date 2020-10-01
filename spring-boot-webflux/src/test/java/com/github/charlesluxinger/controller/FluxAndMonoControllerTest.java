package com.github.charlesluxinger.controller;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@WebFluxTest
@DirtiesContext
class FluxAndMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void returnFlux() {
        var integerFlux = webTestClient
                .get()
                .uri("/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier
                .create(integerFlux)
                .expectSubscription()
                .expectNext(1,2,3,4)
                .verifyComplete();
    }

    @Test
    void returnFluxWithoutStepVerifier() {
        webTestClient
            .get()
                .uri("/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Integer.class)
                .hasSize(4);
    }

    @Test
    void returnFluxWithAssertions() {
        var expectedList = List.of(1, 2, 3, 4);

        var resultList = webTestClient
                .get()
                .uri("/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Integer.class)
                .returnResult()
                .getResponseBody();

        assertEquals(expectedList, resultList);
    }

    @Test
    void returnFluxWithConsumeWith() {
        var expectedList = List.of(1, 2, 3, 4);

        var resultList = webTestClient
                .get()
                .uri("/flux")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Integer.class)
                .consumeWith(response ->  assertEquals(expectedList, response.getResponseBody()));


    }

    @Test
    void returnFluxStream() {
        var longFlux = webTestClient
                .get()
                .uri("/flux")
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier
                .create(longFlux)
                .expectNext(0L, 1L,2L,3L)
                .thenCancel()
                .verify();
    }

    @Test
    void returnMono() {
        var expected = 1;
        webTestClient
                .get()
                .uri("/mono")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Integer.class)
                .consumeWith(response -> assertEquals(expected, response.getResponseBody()));
    }
}