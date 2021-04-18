package com.a6raywa1cher.ostasks.tsk3;

import com.a6raywa1cher.ostasks.tsk3.notifier.DoubleMutexNotifier;
import com.a6raywa1cher.ostasks.tsk3.stringtunnel.MemoryMappedStringTunnel;
import com.a6raywa1cher.ostasks.tsk3.tunnel.DefaultTunnel;
import com.a6raywa1cher.ostasks.tsk3.tunnel.Tunnel;
import io.github.hcoona.concurrent.NamedMutex;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ChatApplicationShit {
    public static final String MASTER_OUTPUT_MUTEX = "masterOutputMutex1";
    public static final String SLAVE_OUTPUT_MUTEX = "slaveOutputMutex1";
    public static final String BUFFER_LOCATION = ".chat.buffer";
    public static final String LOCK_LOCATION = ".chat.lock";
    public static final int BUFFER_SIZE = 4096;

    private static NamedMutex forwardHandshake(String inputMutexName) throws Exception {
        while (true) {
            NamedMutex inputMutex = NamedMutex.newInstance(inputMutexName);
            boolean t = inputMutex.waitOne(100, TimeUnit.MICROSECONDS);
            if (t) {
                Thread.sleep(1000);
            } else {
                System.out.println("Forward!");
                return inputMutex;
            }
            inputMutex.close();
        }
    }

    private static NamedMutex backwardHandshake(String outputMutexName) throws Exception {
        NamedMutex outputMutex = NamedMutex.newInstance(outputMutexName);
        outputMutex.waitOne();
        System.out.println("Backward!");
        return outputMutex;
    }

    public static Pair<NamedMutex, NamedMutex> makeHandshake(String inputMutexName, String outputMutexName, boolean master) throws Exception {
        if (master) {
            NamedMutex inputMutex = forwardHandshake(inputMutexName);
            NamedMutex outputMutex = backwardHandshake(outputMutexName);
            return Pair.of(inputMutex, outputMutex);
        } else {
            NamedMutex outputMutex = backwardHandshake(outputMutexName);
            NamedMutex inputMutex = forwardHandshake(inputMutexName);
            return Pair.of(inputMutex, outputMutex);
        }
    }

    public static void main(String[] args) throws Exception {
        try (MasterSlaveLock masterSlaveLock = new MasterSlaveLock(LOCK_LOCATION)) {
            boolean master = masterSlaveLock.isMaster();
            System.out.println("Got " + (master ? "master" : "slave") + " role");

            String inputMutexName = master ? SLAVE_OUTPUT_MUTEX : MASTER_OUTPUT_MUTEX;
            String outputMutexName = master ? MASTER_OUTPUT_MUTEX : SLAVE_OUTPUT_MUTEX;
            Pair<NamedMutex, NamedMutex> pair = makeHandshake(inputMutexName, outputMutexName, !master);
            NamedMutex inputMutex = pair.getLeft();
            NamedMutex outputMutex = pair.getRight();
            System.out.println("Handshake completed");

            try (Tunnel tunnel = new DefaultTunnel(
                    new MemoryMappedStringTunnel(BUFFER_LOCATION, BUFFER_SIZE),
                    new DoubleMutexNotifier(inputMutex, inputMutexName, outputMutex, outputMutexName));
                 Scanner scanner = new Scanner(System.in)) {

                tunnel.addInputListener(s -> {
                    if (!s.startsWith("/"))
                        System.out.println("GOT MESSAGE: " + s);
                });
                tunnel.addInputListener(s -> {
                    if (s.equals("/exit")) {
                        System.out.println("Pipe closed");
                        System.exit(0);
                    }
                });
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
