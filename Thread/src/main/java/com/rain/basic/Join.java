package com.rain.basic;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j(topic = "c.Join")
public class Join {
    static int i = 10;

    public static void main(String[] args) throws InterruptedException {

        log.info("begin");

        Thread t = new Thread("t") {
            @Override
            public void run() {
                log.info("begin");
                Sleeper.sleep(1);
                log.info("end");
                i = 20;
            }
        };

        t.start();
        t.join();
        log.info("end");
        log.info("i = {}", i);


        // test2();
    }

    static int r = 0;
    static int r1 = 0;
    static int r2 = 0;

    private static void test2() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            Sleeper.sleep(1);
            r1 = 10;
        });
        Thread t2 = new Thread(() -> {
            Sleeper.sleep(2);
            r2 = 20;
        });
        t1.start();
        t2.start();

        long start = System.currentTimeMillis();
        log.info("join begin");
        t2.join();
        log.info("t2 join end");
        t1.join();
        log.info("t1 join end");
        long end = System.currentTimeMillis();
        log.info("r1: {} r2: {} cost: {}", r1, r2, end - start);
    }
}
