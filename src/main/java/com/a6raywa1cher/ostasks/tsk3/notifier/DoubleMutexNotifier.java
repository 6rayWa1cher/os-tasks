package com.a6raywa1cher.ostasks.tsk3.notifier;

import io.github.hcoona.concurrent.NamedMutex;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log
public class DoubleMutexNotifier implements Notifier, AutoCloseable {
    private final NamedMutex inputMutex;
    private final String inputMutexName;
    private final NamedMutex outputMutex;
    private final String outputMutexName;

    private final List<Runnable> listeners;

    public DoubleMutexNotifier(String inputMutexName, String outputMutexName) throws Exception {
        this.inputMutex = NamedMutex.newInstance(false, inputMutexName);
        this.inputMutexName = inputMutexName;
        this.outputMutex = NamedMutex.newInstance(false, outputMutexName);
        this.outputMutexName = outputMutexName;
        this.listeners = new ArrayList<>();
    }

    public DoubleMutexNotifier(NamedMutex inputMutex, String inputMutexName, NamedMutex outputMutex, String outputMutexName) {
        this.inputMutex = inputMutex;
        this.inputMutexName = inputMutexName;
        this.outputMutex = outputMutex;
        this.outputMutexName = outputMutexName;
        this.listeners = new ArrayList<>();
    }

    private void $startListening() {
        try {
            while (true) {
                log.info("listening " + inputMutexName);
                inputMutex.waitOne();
                log.info("got signal from " + inputMutexName);
                listeners.forEach(Runnable::run);
                inputMutex.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    @SneakyThrows
    public void startListening() {
        new Thread(this::$startListening).start();
    }

    @Override
    public void notifyExternalListeners() {
        try {
            log.info("Notifying " + outputMutexName);
            outputMutex.release();
            Thread.sleep(100);
            log.info("Notified " + outputMutexName);
            outputMutex.waitOne();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void addLocalListener(Runnable listener) {
        listeners.add(listener);
    }

    @Override
    public void close() throws Exception {
        inputMutex.close();
        outputMutex.close();
    }
}
