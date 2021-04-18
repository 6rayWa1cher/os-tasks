package com.a6raywa1cher.ostasks.tsk3.notifier;

import java.util.function.Consumer;

public interface Notifier extends AutoCloseable {
    void startListening();

    void notifyExternalListeners();

    void addLocalListener(Runnable listener);
}
