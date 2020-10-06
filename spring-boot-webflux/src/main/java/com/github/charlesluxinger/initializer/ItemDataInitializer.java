package com.github.charlesluxinger.initializer;

import com.github.charlesluxinger.document.Item;
import com.github.charlesluxinger.repository.ItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@AllArgsConstructor
@Profile("dev")
public class ItemDataInitializer implements CommandLineRunner {

    private final ItemRepository itemRepository;

    @Override
    public void run(String... args) {
        initialDataSetUp();
    }

    public List<Item> data() {
        return List.of(new Item(null, "Samsung TV", 399.99),
                new Item(null, "LG TV", 329.99),
                new Item(null, "Apple Watch", 349.99),
                new Item("ABC", "Beats HeadPhones", 19.99));
    }

    private void initialDataSetUp() {
        itemRepository.deleteAll()
                .thenMany(Flux.fromIterable(data()))
                .flatMap(itemRepository::save);
    }

}
