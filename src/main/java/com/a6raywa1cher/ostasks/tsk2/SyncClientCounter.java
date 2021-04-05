package com.a6raywa1cher.ostasks.tsk2;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SyncClientCounter extends AbstractClientCounter {
    private final Lock lock;
    private int clients = 0;

    public SyncClientCounter(boolean decrement) {
        super(decrement);
        this.lock = new ReentrantLock();
    }

    @Override
    protected void increment() {
        lock.lock();
        try {
            clients++;
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void decrement() {
        lock.lock();
        try {
            clients--;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getClients() {
        return this.clients;
    }
}
