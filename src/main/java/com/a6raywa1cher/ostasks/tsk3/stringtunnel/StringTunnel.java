package com.a6raywa1cher.ostasks.tsk3.stringtunnel;

public interface StringTunnel extends AutoCloseable {
    void send(String message);

    String read();
}
