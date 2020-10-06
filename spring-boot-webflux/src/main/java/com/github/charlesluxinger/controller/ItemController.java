package com.github.charlesluxinger.controller;

import com.github.charlesluxinger.document.Item;
import com.github.charlesluxinger.repository.ItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@AllArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;

    @GetMapping("/items")
    public Flux<Item> getAllItems(){
        return itemRepository.findAll();
    }

}