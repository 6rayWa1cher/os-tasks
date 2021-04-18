package com.a6raywa1cher.ostasks.tsk3.tunnel;

import com.a6raywa1cher.ostasks.tsk3.notifier.Notifier;

import java.util.function.Consumer;

public interface Tunnel extends AutoCloseable {
    void start();

    void send(String message);

    void addInputListener(Consumer<String> listener);
}
