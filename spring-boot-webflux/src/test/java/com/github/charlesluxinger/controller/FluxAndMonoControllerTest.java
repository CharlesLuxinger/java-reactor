package com.github.charlesluxinger.controller;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@WebFluxTest
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
    void returnFluxStreamWithAssertions() {
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
    void returnFluxStreamWithConsumeWith() {
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
}