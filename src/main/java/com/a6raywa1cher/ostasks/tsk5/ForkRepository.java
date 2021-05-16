package com.a6raywa1cher.ostasks.tsk5;

import org.apache.commons.lang3.tuple.Pair;

public interface ForkRepository {
    void start() throws IllegalStateException;

    Pair<Fork, Fork> getTwoForks(Philosopher philosopher) throws InterruptedException;

    void returnFork(Fork fork);
}
