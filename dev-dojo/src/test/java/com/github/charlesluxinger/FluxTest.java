package com.github.charlesluxinger;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

@Slf4j
public class FluxTest {

	@Test
	public void fluxSubscriberString() {
		var flux = Flux.just("Charles", "Manuel", "Maria")
								  .log();

		StepVerifier.create(flux)
					.expectNext("Charles", "Manuel", "Maria")
					.verifyComplete();
	}

	@Test
	public void fluxSubscriberNumbers() {
		var flux = Flux.range(1, 5)
								   .log();

		StepVerifier.create(flux)
					.expectNext(1,2,3,4, 5)
					.verifyComplete();
	}

	@Test
	public void fluxSubscriberFromList() {
		var flux = Flux.fromIterable(List.of(1,2,3,4,5))
								   .log();

		StepVerifier.create(flux)
				.expectNext(1,2,3,4, 5)
				.verifyComplete();
	}

	@Test
	public void fluxSubscriberNumbersError() {
		var flux = Flux.range(1, 5)
								   .log()
								   .map(i -> {
									    if (i == 4) {
									        throw new IndexOutOfBoundsException("Index error");
									    }

									    return i;
								   });

		flux.subscribe(i -> log.info("Number {}", i),
					   Throwable::printStackTrace,
					   ()-> log.info("DONE"));

		StepVerifier.create(flux)
				.expectNext(1,2,3)
				.expectError(IndexOutOfBoundsException.class)
				.verify();
	}

	@Test
	public void fluxSubscriberNumbersUglyBackpressure() {
		var flux = Flux.range(1, 10)
								   .log();

		flux.subscribe(new Subscriber<>() {
			private int count = 0;
			private Subscription subscription;
			private final int requestCount = 2;

			@Override
			public void onSubscribe(Subscription subscription) {
				this.subscription = subscription;
				subscription.request(2);
			}

			@Override
			public void onNext(Integer integer) {
				count++;
				if (count >= requestCount){
					count = 0;
					subscription.request(requestCount);
				}
			}

			@Override
			public void onError(Throwable throwable) {

			}

			@Override
			public void onComplete() {

			}
		});

		StepVerifier.create(flux)
				.expectNext(1,2,3,4,5,6,7,8,9,10)
				.expectError(IndexOutOfBoundsException.class)
				.verify();
	}

	@Test
	public void fluxSubscriberNumbersNotSoUglyBackpressure() {
		var flux = Flux.range(1, 10)
								   .log();

		flux.subscribe(new BaseSubscriber<>() {
			private int count = 0;
			private final int requestCount = 2;

			@Override
			protected void hookOnSubscribe(Subscription subscription) {
				request(requestCount);
			}

			@Override
			protected void hookOnNext(Integer value) {
				count++;
				if (count >= requestCount){
					count = 0;
					request(requestCount);
				}
			}
		});

		StepVerifier.create(flux)
				.expectNext(1,2,3,4,5,6,7,8,9,10)
				.expectError(IndexOutOfBoundsException.class)
				.verify();
	}

	@Test
	public void fluxSubscriberNumbersPrettyBackpressure() {
		var flux = Flux.range(1, 10)
				.log()
				.limitRate(3);

		flux.subscribe(i -> log.info("Number {}", i));

		StepVerifier.create(flux)
				.expectNext(1,2,3,4,5,6,7,8,9,10)
				.expectError(IndexOutOfBoundsException.class)
				.verify();
	}

	@Test
	public void fluxSubscriberIntervalOne() throws InterruptedException {
		var flux = Flux.interval(Duration.ofMillis(100))
				.take(10)
				.log();

		flux.subscribe(i -> log.info("Number {}", i));

		Thread.sleep(3000);
	}

	@Test
	public void fluxSubscriberIntervalTwo() {
		StepVerifier.withVirtualTime(this::createInterval)
				.expectSubscription()
				.thenAwait(Duration.ofDays(2))
				.expectNext(0L)
				.expectNext(1L)
				.thenCancel()
				.verify();
	}

	@Test
	public void connectableFlux() throws InterruptedException {
		var flux = Flux.range(1, 10)
				.log()
				.delayElements(Duration.ofMillis(100))
				.publish();

//		flux.connect();

//		log.info("Thread sleeping for 300ms");

//		Thread.sleep(300);

//		flux.subscribe(i -> log.info("Sub1 number {}", i));

//		log.info("Thread sleeping for 200ms");

//		Thread.sleep(200);

//		flux.subscribe(i -> log.info("Sub2 number {}", i));

		StepVerifier
				.create(flux)
				.then(flux::connect)
				.thenConsumeWhile(i -> i <= 5)
				.expectNext(6,7,8,9,10)
				.expectComplete()
				.verify();

	}

	@Test
	public void connectableFluxAutoConnect() throws InterruptedException {
		var flux = Flux.range(1, 10)
				.log()
				.delayElements(Duration.ofMillis(100))
				.publish()
				.autoConnect(2);

		StepVerifier
				.create(flux)
				.then(flux::subscribe)
				.expectNext(1,2,3,4,5,6,7,8,9,10)
				.expectComplete()
				.verify();
	}

	private Flux<Long> createInterval() {
		return Flux.interval(Duration.ofDays(1))
				.take(10)
				.log();
	}
}
