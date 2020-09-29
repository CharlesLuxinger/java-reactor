package com.github.charlesluxinger.repository;

import com.github.charlesluxinger.document.Item;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ItemRepository extends ReactiveMongoRepository<Item, String> {}