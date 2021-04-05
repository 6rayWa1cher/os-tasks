package com.a6raywa1cher.ostasks.tsk2;

public class UnsafeClientCounter extends AbstractClientCounter {
    private int clients = 0;

    protected UnsafeClientCounter(boolean decrement) {
        super(decrement);
    }

    @Override
    void increment() {
        clients++;
    }

    @Override
    void decrement() {
        clients--;
    }

    @Override
    public int getClients() {
        return this.clients;
    }
}
