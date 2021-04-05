package com.a6raywa1cher.ostasks.tsk2;

import java.util.LinkedList;
import java.util.List;

public class TestBench {
    public static final int CLIENT_COUNT = 1000;
    public static final int CLIENT_TIMES = 100;

    private static void test(AbstractClientCounter abstractClientCounter) {
        System.out.println("Starting with " + abstractClientCounter.getClass().getSimpleName());
        long start = System.currentTimeMillis();
        List<Client> clientList = new LinkedList<>();
        for (int i = 0; i < CLIENT_COUNT; i++) {
            Client client = new Client(abstractClientCounter);
            client.makeJob(CLIENT_TIMES);
            clientList.add(client);
        }
        clientList.forEach(Client::join);
        long end = System.currentTimeMillis();
        System.out.println("Expecting " + (CLIENT_TIMES * CLIENT_COUNT) + ", got " + abstractClientCounter.getClients() + ", time " + (end - start) + "ms");
    }

    public static void main(String[] args) {
        System.out.println("---NO DECREMENT---");
        test(new UnsafeClientCounter(false));
        test(new SyncClientCounter(false));
        test(new AtomicClientCounter(false));
        System.out.println("---WITH DECREMENT---");
        test(new UnsafeClientCounter(true));
        test(new SyncClientCounter(true));
        test(new AtomicClientCounter(true));
    }
}
