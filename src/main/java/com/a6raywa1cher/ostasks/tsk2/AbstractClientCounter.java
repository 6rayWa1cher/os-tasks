package com.a6raywa1cher.ostasks.tsk2;

public abstract class AbstractClientCounter implements ClientCounter {
    private final boolean decrement;

    protected AbstractClientCounter(boolean decrement) {
        this.decrement = decrement;
    }

    abstract void increment();

    abstract void decrement();

    public void connect(Runnable runnable) {
        increment();
        runnable.run();
        if (decrement) decrement();
    }
}
