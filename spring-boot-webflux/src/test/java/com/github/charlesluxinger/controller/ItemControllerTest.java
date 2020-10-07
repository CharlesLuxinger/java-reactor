package com.github.charlesluxinger.controller;

import com.github.charlesluxinger.document.Item;
import com.github.charlesluxinger.repository.ItemRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
@AutoConfigureWebTestClient
public class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    public List<Item> data() {
        return List.of(new Item(null, "Samsung TV", 399.99),
                new Item(null, "LG TV", 329.99),
                new Item(null, "Apple Watch", 349.99),
                new Item("ABC", "Beats HeadPhones", 149.99));
    }

    @Before
    public void setUp(){
        itemRepository.deleteAll()
                .thenMany(Flux.fromIterable(data()))
                .flatMap(itemRepository::save)
                .doOnNext((item -> {
                    System.out.println("Inserted item is : " + item);
                }))
                .blockLast();
    }

    @Test
    public void getAllItems(){
        webTestClient
                .get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Item.class)
                .hasSize(4);
    }

    @Test
    public void getAllItemsApproach2(){
        webTestClient
            .get()
            .uri("/items")
            .exchange()
            .expectStatus()
                .isOk()
            .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Item.class)
                .hasSize(4)
                .consumeWith((response) -> {
                    List<Item> items =  response.getResponseBody();
                    items.forEach(item -> assertTrue(item.getId() != null));
            });
    }

    @Test
    public void getAllItemsApproach3(){
        Flux<Item> itemsFlux =
            webTestClient
                .get()
                .uri("/items")
                .exchange()
                .expectStatus()
                        .isOk()
                .expectHeader()
                        .contentType(MediaType.APPLICATION_JSON)
                .returnResult(Item.class)
                .getResponseBody();

        StepVerifier
                .create(itemsFlux)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void getOneItem(){
        webTestClient
            .get()
            .uri("/items/{id}","ABC")
            .exchange()
            .expectStatus()
                .isOk()
            .expectBody()
                .jsonPath("$.price", 149.99);
    }

    @Test
    public void getOneItem_notFound(){
        webTestClient
            .get()
            .uri("/items/{id}","DEF")
            .exchange()
            .expectStatus()
                .isNotFound();
    }

    @Test
    public void createItem(){
        webTestClient
            .post()
            .uri("/items")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(new Item(null, "Iphone X", 999.99)), Item.class)
            .exchange()
            .expectStatus()
                .isCreated()
            .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.description").isEqualTo("Iphone X")
                .jsonPath("$.price").isEqualTo(999.99);
    }

    @Test
    public void deleteItem(){
        webTestClient
            .delete()
            .uri("/items/{id}","ABC")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
                .isNoContent()
            .expectBody(Void.class);
    }

    @Test
    public void updateItem(){
        var newPrice = 129.99;

        webTestClient
            .put()
            .uri("/items/{id}","ABC")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(new Item(null,"Beats HeadPhones", newPrice)), Item.class)
            .exchange()
            .expectStatus()
                .isOk()
            .expectBody()
                .jsonPath("$.price", newPrice);
    }

    @Test
    public void updateItemNotFound(){
        var newPrice = 129.99;

        webTestClient
            .put()
            .uri("/items/{id}","DEF")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(new Item(null,"Beats HeadPhones", newPrice)), Item.class)
            .exchange()
            .expectStatus()
                .isNotFound()
            .expectBody(Void.class);
    }

}