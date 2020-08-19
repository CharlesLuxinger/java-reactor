package com.github.charlesluxinger;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MonoTest {

	@BeforeAll
	public static void setUp() {
		BlockHound.install();
	}

	@Test
	public void blockHoundWorks() {
		try {
			FutureTask<?> task = new FutureTask<>(() -> {
				Thread.sleep(0);
				return "";
			});
			Schedulers.parallel().schedule(task);

			task.get(10, TimeUnit.SECONDS);
			Assertions.fail("should fail");
		} catch (Exception e) {
			Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
		}
	}

	@Test
	public void monoSubscriber() {
		var name = "Charles Luxinger";

		var mono = Mono.just(name)
									.log();

		mono.subscribe();

		StepVerifier.create(mono)
					.expectNext(name)
					.verifyComplete();
	}

	@Test
	public void monoSubscriberConsumer() {
		var name = "Charles Luxinger";

		var mono = Mono.just(name)
									.log();

		mono.subscribe(System.out::println);

		StepVerifier.create(mono)
					.expectNext(name)
					.verifyComplete();
	}

	@Test
	public void monoSubscriberConsumerError() {
		var name = "Charles Luxinger";

		var mono = Mono.just(name)
									.map(s -> {throw new RuntimeException("Error");});

		mono.subscribe(System.out::println, Throwable::printStackTrace);

		StepVerifier.create(mono)
					.expectError(RuntimeException.class)
					.verify();
	}

	@Test
	public void monoSubscriberConsumerComplete() {
		var name = "Charles Luxinger";

		var mono = Mono.just(name)
									.log()
									.map(String::toUpperCase);

		mono.subscribe(System.out::println,
					   Throwable::printStackTrace,
					   () -> log.info("Finished"));

		StepVerifier.create(mono)
					.expectNext(name.toUpperCase())
					.verifyComplete();
	}

	@Test
	public void monoSubscriberConsumerSubscription() {
		var name = "Charles Luxinger";

		var mono = Mono.just(name)
				.log()
				.map(String::toUpperCase);

		mono.subscribe(System.out::println,
					   Throwable::printStackTrace,
					   () -> log.info("Finished"),
                       subscription -> subscription.request(5));
	}

	@Test
	public void monoDoOnMethods() {
		var name = "Charles Luxinger";

		var mono = Mono.just(name)
									.log()
									.map(String::toUpperCase)
									.doOnSubscribe(subscription -> log.info("Subscribed"))
									.doOnRequest(longNumber -> log.info("Do something..."))
									.doOnNext(s -> log.info("Next {}", s))
									.doOnSuccess(s -> log.info("Success executed"));

		mono.subscribe(System.out::println,
						Throwable::printStackTrace,
						() -> log.info("Finished"));
	}

	@Test
	public void monoDoOnError() {
		var mono = Mono.error(new IllegalAccessException("Something wrong"))
									.doOnError(e -> log.error("Error: {}", e.getMessage()))
									.doOnNext(s -> log.info("Next: ", s))
									.log();

		StepVerifier.create(mono)
					.expectError(IllegalAccessException.class)
					.verify();
	}

	@Test
	public void monoDoOnErrorResume() {
		var mono = Mono.error(new IllegalAccessException("Something wrong"))
				.doOnError(e -> log.error("Error: {}", e.getMessage()))
				.onErrorResume(s -> {
					log.info("Next: ", s);

					return Mono.just("Something");
				})
				.log();

		StepVerifier.create(mono)
					.expectNext("Something")
					.verifyComplete();
	}

	@Test
	public void monoDoOnErrorReturn() {
		var mono = Mono.error(new IllegalAccessException("Something wrong"))
				.doOnError(e -> log.error("Error: {}", e.getMessage()))
				.onErrorReturn("return")
				.log();

		StepVerifier.create(mono)
				.expectNext("return")
				.verifyComplete();
	}

}
