package com.a6raywa1cher.ostasks.tsk2;

import lombok.SneakyThrows;

public class Client {
    private final AbstractClientCounter abstractClientCounter;
    private Thread thread;

    public Client(AbstractClientCounter abstractClientCounter) {
        this.abstractClientCounter = abstractClientCounter;
    }

    private void innerMakeJob() {
        abstractClientCounter.connect(() -> {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void makeJob(int times) {
        thread = new Thread(() -> {
            for (int i = 0; i < times; i++) {
                this.innerMakeJob();
            }
        });
        thread.start();
    }

    @SneakyThrows
    public void join() {
        thread.join();
    }
}
