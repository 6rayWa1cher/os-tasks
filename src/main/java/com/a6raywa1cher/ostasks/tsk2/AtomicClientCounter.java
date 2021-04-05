package com.a6raywa1cher.ostasks.tsk2;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicClientCounter extends AbstractClientCounter {
    private final AtomicInteger atomicInteger;

    public AtomicClientCounter(boolean decrement) {
        super(decrement);
        atomicInteger = new AtomicInteger(0);
    }

    @Override
    protected void increment() {
        atomicInteger.getAndIncrement();
    }

    @Override
    protected void decrement() {
        atomicInteger.getAndDecrement();
    }

    @Override
    public int getClients() {
        return atomicInteger.get();
    }
}
