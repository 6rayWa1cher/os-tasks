package com.a6raywa1cher.ostasks.tsk5;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class Restaurant {
    public static void main(String[] args) {
        ForkRepository forkRepository = new WaiterBasedForkRepository(List.of(
                new Fork("sharp"),
                new Fork("dull"),
                new Fork("steel"),
                new Fork("new"),
                new Fork("golden")
        ));
        List<Philosopher> philosophers = IntStream.range(0, 5)
                .mapToObj(i -> new Philosopher(
                        "philosopher#" + i,
                        forkRepository,
                        100
                ))
                .collect(Collectors.toList());
        List<Thread> threads = philosophers.stream()
                .map(
                        p -> new Thread(() -> {
                            while (true) {
                                try {
                                    p.dinner();
                                } catch (InterruptedException e) {
                                    log.error("Interrupted. Shutting down the JVM", e);
                                    System.exit(1);
                                }
                            }
                        })
                )
                .peek(Thread::start)
                .collect(Collectors.toList());
        forkRepository.start();
        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(5000);
                    log.info("PHILOSOPHER REPORT");
                    for (Philosopher p : philosophers) {
                        log.info("R: {} eated {} times", p.getName(), p.getDinnerCount());
                    }
                }
            } catch (InterruptedException e) {
                log.error("Interrupted. Shutting down the JVM", e);
                System.exit(1);
            }
        }).start();
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.error("Interrupted. Shutting down the JVM", e);
                System.exit(1);
            }
        });
    }
}
