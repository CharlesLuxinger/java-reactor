package com.github.charlesluxinger;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class OperatorsTest {

	@Test
	public void subscribedOnSimple() {
		var flux = Flux.range(1, 4)
				.map(i -> {
					log.info("Map 1: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				})
				.subscribeOn(Schedulers.single())
				.map(i -> {
					log.info("Map 2: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				});

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1,2,3,4)
				.verifyComplete();
	}

	@Test
	public void publishedOnSimple() {
		var flux = Flux.range(1, 4)
				.map(i -> {
					log.info("Map 1: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				})
				.publishOn(Schedulers.boundedElastic())
				.map(i -> {
					log.info("Map 2: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				});

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1,2,3,4)
				.verifyComplete();
	}

	@Test
	public void multipleSubscribeOnSimple() {
		var flux = Flux.range(1, 4)
				.subscribeOn(Schedulers.single())
				.map(i -> {
					log.info("Map 1: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				})
				.subscribeOn(Schedulers.boundedElastic())
				.map(i -> {
					log.info("Map 2: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				});

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1,2,3,4)
				.verifyComplete();
	}

	@Test
	public void multiplePublishedOnSimple() {
		var flux = Flux.range(1, 4)
				.publishOn(Schedulers.single())
				.map(i -> {
					log.info("Map 1: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				})
				.publishOn(Schedulers.boundedElastic())
				.map(i -> {
					log.info("Map 2: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				});

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1,2,3,4)
				.verifyComplete();
	}

	@Test
	public void publishedAndSubscribeOnSimple() {
		var flux = Flux.range(1, 4)
				.publishOn(Schedulers.single())
				.map(i -> {
					log.info("Map 1: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				})
				.subscribeOn(Schedulers.boundedElastic())
				.map(i -> {
					log.info("Map 2: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				});

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1,2,3,4)
				.verifyComplete();
	}

	@Test
	public void subscribeAndPublishedOnSimple() {
		var flux = Flux.range(1, 4)
				.subscribeOn(Schedulers.single())
				.map(i -> {
					log.info("Map 1: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				})
				.publishOn(Schedulers.boundedElastic())
				.map(i -> {
					log.info("Map 2: Number {} on Thread {} ", i, Thread.currentThread().getName());
					return i;
				});

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext(1,2,3,4)
				.verifyComplete();
	}

	@Test
	public void subscribeOnIO(){
		var mono = Mono.fromCallable(() -> Files.readAllLines(Path.of("text-file.txt")))
				.log()
				.subscribeOn(Schedulers.boundedElastic());

		mono.subscribe(s -> log.info("{}", s));

		StepVerifier.create(mono)
				.expectSubscription()
				.thenConsumeWhile(l -> {
					Assertions.assertFalse(l.isEmpty());
					return true;
				})
				.verifyComplete();
	}

	@Test
	public void switchIfEmptyOperator() {
		var flux = emptyFlux()
				.switchIfEmpty(Flux.just("Not Empty"))
				.log();

		StepVerifier.create(flux)
				.expectSubscription()
				.expectNext("Not Empty")
				.expectComplete()
				.verify();
	}

	@Test
	public void deferOperator() throws InterruptedException {
		var just = Mono.just(System.currentTimeMillis());
		var defer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));

		just.subscribe(t -> log.info("Time just: {}", t));
		Thread.sleep(100);
		just.subscribe(t -> log.info("Time just: {}", t));

		defer.subscribe(t -> log.info("Time defer: {}", t));
		Thread.sleep(100);
		defer.subscribe(t -> log.info("Time defer: {}", t));

		var atomicLong = new AtomicLong();

		defer.subscribe(atomicLong::set);

		Assertions.assertTrue(atomicLong.get() > 0);

	}

	@Test
	public void concatOperator() {
		var flux1 = Flux.just("a", "b");
		var flux2 = Flux.just("c", "d");

		var concatFlux = Flux.concat(flux1, flux2).log();

		StepVerifier
				.create(concatFlux)
				.expectSubscription()
				.expectNext("a", "b", "c", "d")
				.expectComplete()
				.verify();
	}

	@Test
	public void concatWithOperator() {
		var flux1 = Flux.just("a", "b");
		var flux2 = Flux.just("c", "d");

		var concatFlux = flux1.concatWith(flux2);

		StepVerifier
				.create(concatFlux)
				.expectSubscription()
				.expectNext("a", "b", "c", "d")
				.expectComplete()
				.verify();
	}

	@Test
	public void concatDelayErrorOperator() {
		var flux1 = Flux
				.just("a", "b")
				.map(s -> {
					if (s.equals("b"))
						throw new IllegalArgumentException();

					return s;
				});
		var flux2 = Flux.just("c", "d");

		var concatFlux = Flux.concatDelayError(flux1, flux2).log();

		StepVerifier
				.create(concatFlux)
				.expectSubscription()
				.expectNext("a","c", "d")
				.expectError()
				.verify();
	}

	@Test
	public void combineLatestOperator() {
		var flux1 = Flux.just("a", "b");
		var flux2 = Flux.just("c", "d");

		var combineLatest = Flux
				.combineLatest(flux1, flux2, (a, b) -> a.toUpperCase() + b.toUpperCase())
				.log();

		StepVerifier
				.create(combineLatest)
				.expectSubscription()
				.expectNext("BC", "BD")
				.expectComplete()
				.verify();
	}

	@Test
	public void mergeOperator() {
		var flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
		var flux2 = Flux.just("c", "d");

		var mergeFlux = Flux
				.merge(flux1, flux2)
				.delayElements(Duration.ofMillis(200))
				.log();

		StepVerifier
				.create(mergeFlux)
				.expectSubscription()
				.expectNext("c", "d", "a", "b")
				.expectComplete()
				.verify();
	}

	@Test
	public void mergeWithOperator() {
		var flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
		var flux2 = Flux.just("c", "d");

		var mergeFlux = flux1
				.mergeWith(flux2)
				.delayElements(Duration.ofMillis(200))
				.log();

		StepVerifier
				.create(mergeFlux)
				.expectSubscription()
				.expectNext("c", "d", "a", "b")
				.expectComplete()
				.verify();
	}

	@Test
	public void mergeSequentialOperator() {
		var flux1 = Flux.just("a", "b");
		var flux2 = Flux.just("c", "d");

		var mergeFlux = Flux
				.merge(flux1, flux2, flux1)
				.log();

		StepVerifier
				.create(mergeFlux)
				.expectSubscription()
				.expectNext("a", "b", "c", "d", "a", "b")
				.expectComplete()
				.verify();
	}

	@Test
	public void mergeDelayErrorOperator() {
		var flux1 = Flux
				.just("a", "b")
				.map(s -> {
					if (s.equals("b"))
						throw new IllegalArgumentException();

					return s;
				});
		var flux2 = Flux.just("c", "d");

		var mergeFlux = Flux
				.mergeDelayError(1, flux1, flux2)
				.log();

		StepVerifier
				.create(mergeFlux)
				.expectSubscription()
				.expectNext( "a", "c", "d")
				.expectError()
				.verify();
	}

	private Flux<Object> emptyFlux() {
		return Flux.empty();
	}
}
