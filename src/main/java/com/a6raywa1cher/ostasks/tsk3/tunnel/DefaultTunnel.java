package com.a6raywa1cher.ostasks.tsk3.tunnel;

import com.a6raywa1cher.ostasks.tsk3.notifier.Notifier;
import com.a6raywa1cher.ostasks.tsk3.stringtunnel.StringTunnel;
import lombok.extern.java.Log;

import java.util.function.Consumer;

@Log
public class DefaultTunnel implements Tunnel {
    private final StringTunnel tunnel;

    private final Notifier notifier;

    public DefaultTunnel(StringTunnel tunnel, Notifier notifier) {
        this.tunnel = tunnel;
        this.notifier = notifier;
    }

    @Override
    public void start() {
        notifier.startListening();
    }

    @Override
    public void send(String message) {
        log.info("Sending message " + message);
        tunnel.send(message);
        notifier.notifyExternalListeners();
    }

    @Override
    public void addInputListener(Consumer<String> listener) {
        notifier.addLocalListener(() -> {
            listener.accept(tunnel.read());
        });
    }

    @Override
    public void close() throws Exception {
        notifier.close();
        tunnel.close();
    }
}
