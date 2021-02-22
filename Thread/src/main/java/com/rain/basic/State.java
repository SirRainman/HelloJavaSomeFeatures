package com.rain.basic;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;

@Slf4j(topic = "c.State")
public class State {
    @Test
    public void lifecycleChange() {
        Thread t = new Thread(() -> {
            log.info("t1 enter sleeping ...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, "t1");
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
        log.info("t1's isInterrupted: {}", t.isInterrupted()); // 在sleep期间打断会报错，抛出异常之后会重置中断标识位
    }

    @Test
    public void differentState() {
        Thread t1 = new Thread(() -> {
            log.info("t1 running...");
        }, "t1");

        Thread t2 = new Thread(() -> {
            while (true) { // runnable

            }
        }, "t2");
        t2.start();

        Thread t3 = new Thread(() -> {
            log.info("t3 running...");
        }, "t3");
        t3.start();

        Thread t4 = new Thread(() -> {
            synchronized (Thread.State.class) {
                try {
                    Thread.sleep(1000000); // timed_waiting
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t4");
        t4.start();

        Thread t5 = new Thread(() -> {
            try {
                t2.join(); // waiting
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t5");
        t5.start();

        Thread t6 = new Thread(() -> {
            synchronized (State.class) { // blocked
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t6");
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
