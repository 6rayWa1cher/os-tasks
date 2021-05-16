package com.a6raywa1cher.ostasks.tsk5;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

@Slf4j
public class WaiterBasedForkRepository implements ForkRepository {
    private final Set<Fork> allForks;
    private final Queue<Fork> nonBlockedForks = new ConcurrentLinkedQueue<>();
    private final Queue<Philosopher> concurrentQueue = new ConcurrentLinkedQueue<>();
    private final CountDownLatch consumerLatch = new CountDownLatch(1);
    private final Semaphore supplySemaphore;
    private final Semaphore requestSemaphore = new Semaphore(0);
    private final Semaphore waiterSemaphore = new Semaphore(0);
    private Philosopher greenLight;
    private int requestRound;
    private boolean started = false;

    public WaiterBasedForkRepository(Collection<Fork> forks) {
        this.nonBlockedForks.addAll(forks);
        this.allForks = new HashSet<>(forks);
        this.supplySemaphore = new Semaphore(forks.size());
    }

    @Override
    public void start() throws IllegalStateException {
        if (started) throw new IllegalStateException("started == true");
        started = true;
        new Thread(() -> {
            try {
                $start();
            } catch (InterruptedException e) {
                log.error("Interrupted. Shutting down the JVM", e);
                System.exit(1);
            }
        }, "WaiterT").start();
    }

    private void $start() throws InterruptedException {
        while (true) {
            requestSemaphore.acquire();
            log.trace("waiter: got request");
            supplySemaphore.acquire();
            greenLight = concurrentQueue.poll();
            log.trace("waiter: got fork {} {}", nonBlockedForks.size(), supplySemaphore.availablePermits());
            requestRound++;
            log.trace("waiter: sent response");
            consumerLatch.countDown();
            waiterSemaphore.acquire();
        }
    }

    @Override
    public Pair<Fork, Fork> getTwoForks(Philosopher philosopher) throws InterruptedException {
        synchronized (concurrentQueue) {
            concurrentQueue.add(philosopher);
            concurrentQueue.add(philosopher);
        }
        requestSemaphore.release(2);
        return Pair.of(getFork(philosopher), getFork(philosopher));
    }

    private Fork getFork(Philosopher philosopher) throws InterruptedException {
        while (true) {
            consumerLatch.await();
            if (philosopher.equals(greenLight)) {
                log.trace("{} {} {}", philosopher.getName(), requestRound, nonBlockedForks.size());
                Fork fork = nonBlockedForks.poll();
                greenLight = null;
                waiterSemaphore.release();
                log.trace("released");
                if (fork == null) {
                    throw new RuntimeException();
                }
                fork.setOwner(philosopher);
                return fork;
            }
        }
    }

//    @Override
//    public Fork getFork(Philosopher philosopher) throws InterruptedException {
//        concurrentQueue.add(philosopher);
//        requestSemaphore.release();
//        while (true) {
//            consumerLatch.await();
//            if (philosopher.equals(concurrentQueue.peek())) {
//                concurrentQueue.poll();
//                log.info("{} {} {}", philosopher.getName(), requestRound, nonBlockedForks.size());
//                Fork fork = nonBlockedForks.poll();
//                waiterSemaphore.release();
//                if (fork == null) {
//                    throw new RuntimeException();
//                }
//                fork.setOwner(philosopher);
//                return fork;
//            }
//        }
//    }

    @Override
    public void returnFork(Fork fork) {
        fork.eraseOwner();
        nonBlockedForks.add(fork);
        supplySemaphore.release();
        log.trace("Fork '{}' has returned", fork.getName());
    }

    public Set<Fork> getAllForks() {
        return allForks;
    }
}
