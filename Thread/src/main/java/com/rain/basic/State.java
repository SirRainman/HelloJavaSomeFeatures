package com.rain.basic;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j(topic = "c.State")
public class State {
    public static void main(String[] args) {
        Thread t = new Thread("t1") {
            @Override
            public void run() {
                log.info("t1 enter sleeping ...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        log.info("t1 state: {}", t.getState());
        t.start();
        log.info("t1 state: {}", t.getState());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("t1 state: {}", t.getState());

        log.info("interrupt t1 ...");
        // 在sleep期间打断会报错
        t.interrupt();
        log.info("t1 state: {}", t.getState());
        log.info("t1's isInterrupted: {}", t.isInterrupted());


//        testState();
    }

    public static void testState() {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                log.info("running...");
            }
        };

        Thread t2 = new Thread("t2") {
            @Override
            public void run() {
                while(true) { // runnable

                }
            }
        };
        t2.start();

        Thread t3 = new Thread("t3") {
            @Override
            public void run() {
                log.info("running...");
            }
        };
        t3.start();

        Thread t4 = new Thread("t4") {
            @Override
            public void run() {
                synchronized (State.class) {
                    try {
                        Thread.sleep(1000000); // timed_waiting
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t4.start();

        Thread t5 = new Thread("t5") {
            @Override
            public void run() {
                try {
                    t2.join(); // waiting
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t5.start();

        Thread t6 = new Thread("t6") {
            @Override
            public void run() {
                synchronized (State.class) { // blocked
                    try {
                        Thread.sleep(1000000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t6.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("t1 state {}", t1.getState());
        log.info("t2 state {}", t2.getState());
        log.info("t3 state {}", t3.getState());
        log.info("t4 state {}", t4.getState());
        log.info("t5 state {}", t5.getState());
        log.info("t6 state {}", t6.getState());

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
