package com.a6raywa1cher.ostasks.tsk3;

import com.a6raywa1cher.ostasks.tsk3.notifier.DoubleMutexNotifier;
import com.a6raywa1cher.ostasks.tsk3.stringtunnel.MemoryMappedStringTunnel;
import com.a6raywa1cher.ostasks.tsk3.tunnel.DefaultTunnel;
import com.a6raywa1cher.ostasks.tsk3.tunnel.Tunnel;
import io.github.hcoona.concurrent.NamedMutex;

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.OpenOption;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class ChatApplication {
    public static final String MASTER_OUTPUT_MUTEX = "com.a6raywa1cher.ostasks.tsk3.masterOutputMutex";
    public static final String SLAVE_OUTPUT_MUTEX = "com.a6raywa1cher.ostasks.tsk3.slaveOutputMutex";
    public static final String BUFFER_LOCATION = ".chat.buffer";
    public static final String LOCK_LOCATION = ".chat.lock";
    public static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) throws Exception {
        try (MasterSlaveLock masterSlaveLock = new MasterSlaveLock(LOCK_LOCATION) ) {
            boolean master = masterSlaveLock.isMaster();
            System.out.println("Got " + (master ? "master" : "slave") + " role");

            String inputMutex = master ? SLAVE_OUTPUT_MUTEX : MASTER_OUTPUT_MUTEX;
            String outputMutex = master ? MASTER_OUTPUT_MUTEX : SLAVE_OUTPUT_MUTEX;

            try (Tunnel tunnel = new DefaultTunnel(
                    new MemoryMappedStringTunnel(BUFFER_LOCATION, BUFFER_SIZE),
                    new DoubleMutexNotifier(inputMutex, outputMutex));
                 Scanner scanner = new Scanner(System.in)) {

                Semaphore semaphore = new Semaphore(0);
                tunnel.addInputListener(s -> {
                    if (!s.startsWith("/"))
                        System.out.println("GOT MESSAGE: " + s);
                });
                tunnel.addInputListener(s -> {
                    if (s.equals("/exit")) {
                        System.out.println("Pipe closed");
                        System.exit(0);
                    } else if (s.equals("/init")) {
                        semaphore.release();
                    }
                });
                if (!master) tunnel.send("/init");
                else semaphore.acquire();
                tunnel.start();

                while (true) {
                    String input = scanner.next();
                    System.out.println("Sending...");
                    if ("/exit".equals(input)) break;
                    if (input == null) continue;
                    if (input.startsWith("/")) {
                        System.out.println("Unknown command");
                        continue;
                    }
                    tunnel.send(input);
                }
                tunnel.send("/exit");
            }
        }
    }
}
