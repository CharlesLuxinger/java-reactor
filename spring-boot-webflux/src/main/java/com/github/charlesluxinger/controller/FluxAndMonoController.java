package com.github.charlesluxinger.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class FluxAndMonoController {

    @GetMapping("/flux")
    public Flux<Integer> returnFlux() {
        return Flux
                .just(1,2,3,4)
                .log();
    }

    @GetMapping(value = "/flux", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Integer> returnFluxStream() {
        return Flux
                .just(1,2,3,4)
                .log();
    }

}
