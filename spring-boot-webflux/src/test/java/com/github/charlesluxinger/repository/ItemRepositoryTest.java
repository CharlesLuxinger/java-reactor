package com.github.charlesluxinger.repository;

import com.github.charlesluxinger.document.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@DataMongoTest
@RunWith(SpringRunner.class)
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemReactiveRepository;

    private final List<Item> itemList = Arrays.asList(new Item(null,"Samsung TV",400.0),
            new Item(null,"LG TV",420.0),
            new Item(null,"Apple Watch",299.99),
            new Item("ABC","Bose Headphones",149.99));

    @Before
    public void setUp(){
        itemReactiveRepository
                .deleteAll()
                .thenMany(Flux.fromIterable(itemList))
                .flatMap(itemReactiveRepository::save)
                .doOnNext((item -> System.out.println("Inserted Item is :" + item)))
                .blockLast();
    }

    @Test
    public void getAllItems(){
        StepVerifier
            .create(itemReactiveRepository.findAll())
            .expectSubscription()
            .expectNextCount(4)
            .verifyComplete();
    }

    @Test
    public void getItemByID(){
        StepVerifier
            .create(itemReactiveRepository.findById("ABC"))
            .expectSubscription()
            .expectNextMatches(item -> item.getDescription().equals("Bose Headphones"))
            .verifyComplete();
    }

    @Test
    public void findItemByDescrition() {
        StepVerifier
                .create(itemReactiveRepository.findByDescription("Bose Headphones"))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }
}
