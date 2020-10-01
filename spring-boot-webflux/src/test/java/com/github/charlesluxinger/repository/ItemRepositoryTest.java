package com.github.charlesluxinger.repository;

import com.github.charlesluxinger.document.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@DataMongoTest
@RunWith(SpringRunner.class)
@DirtiesContext
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    private final List<Item> itemList = Arrays.asList(new Item(null,"Samsung TV",400.0),
            new Item(null,"LG TV",420.0),
            new Item(null,"Apple Watch",299.99),
            new Item("ABC","Bose Headphones",149.99));

    @Before
    public void setUp(){
        itemRepository
                .deleteAll()
                .thenMany(Flux.fromIterable(itemList))
                .flatMap(itemRepository::save)
                .doOnNext((item -> System.out.println("Inserted Item is :" + item)))
                .blockLast();
    }

    @Test
    public void getAllItems(){
        StepVerifier
            .create(itemRepository.findAll())
            .expectSubscription()
            .expectNextCount(4)
            .verifyComplete();
    }

    @Test
    public void getItemByID(){
        StepVerifier
            .create(itemRepository.findById("ABC"))
            .expectSubscription()
            .expectNextMatches(item -> item.getDescription().equals("Bose Headphones"))
            .verifyComplete();
    }

    @Test
    public void findItemByDescrition() {
        StepVerifier
            .create(itemRepository.findByDescription("Bose Headphones"))
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void saveItem(){
        Mono<Item> savedItem = itemRepository.save(new Item(null,"Google Home Mini",30.00));

        StepVerifier
            .create(savedItem)
            .expectSubscription()
            .expectNextMatches(i -> (i.getId() != null && i.getDescription().equals("Google Home Mini")))
            .verifyComplete();
    }

    @Test
    public void updateItem(){
        Flux<Item> updatedItem = itemRepository
                .findByDescription("LG TV")
                .map(item -> {
                    item.setPrice(520.00);
                    return item;
                })
                .flatMap(itemRepository::save);

        StepVerifier
            .create(updatedItem)
            .expectSubscription()
            .expectNextMatches(item -> item.getPrice() == 520.00)
            .verifyComplete();
    }

}
