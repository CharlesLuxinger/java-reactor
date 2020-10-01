package com.github.charlesluxinger.repository;

import com.github.charlesluxinger.document.Item;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ItemRepository extends ReactiveMongoRepository<Item, String> {

    Flux<Item> findByDescription(String description);

}