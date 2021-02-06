package com.rain.basic;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;

@Slf4j(topic = "c.Park")
public class Park {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                log.info("park");
                LockSupport.park();
                log.info("interrupted = {}", Thread.interrupted());
//                log.info("isInterrupted = {}", Thread.currentThread().isInterrupted());
            }
        }, "t");
        t.start();

        Sleeper.sleep(1);
        t.interrupt();


        for (int i = 0; i < 5; i++) {
            log.info("unpark");
            LockSupport.unpark(t);
            Sleeper.sleep(1);
        }
    }
}
