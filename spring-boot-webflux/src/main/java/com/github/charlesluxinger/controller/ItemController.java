package com.github.charlesluxinger.controller;

import com.github.charlesluxinger.document.Item;
import com.github.charlesluxinger.repository.ItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;

    @GetMapping("/items")
    public Flux<Item> getAllItems(){
        return itemRepository.findAll();
    }

    @GetMapping("/items/{id}")
    public Mono<ResponseEntity> getOneItem(@PathVariable String id) {
        return itemRepository
                .findById(id)
                .map(ResponseEntity::ok)
                .cast(ResponseEntity.class)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Item> createItem(@RequestBody Item item) {
        return itemRepository.save(item);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteItem(@PathVariable String id) {
        return itemRepository.deleteById(id);
    }

    @PutMapping("/items/{id}")
    public Mono<ResponseEntity> updateItem(@PathVariable String id,
                                           @RequestBody Item item) {
        return itemRepository
                .findById(id)
                .flatMap(currentItem -> {
                    currentItem.setPrice(item.getPrice());
                    currentItem.setDescription(item.getDescription());
                    return itemRepository.save(currentItem);
                })
                .map(ResponseEntity::ok)
                .cast(ResponseEntity.class)
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }

}