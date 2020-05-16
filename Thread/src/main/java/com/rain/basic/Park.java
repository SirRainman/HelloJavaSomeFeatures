package com.rain.basic;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;

@Slf4j(topic = "c.Park")
public class Park {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                log.debug("park");
                LockSupport.park();
                log.debug("interrupted = {}", Thread.interrupted());
//                log.debug("isInterrupted = {}", Thread.currentThread().isInterrupted());
            }
        }, "t");
        t.start();

        Sleeper.sleep(1);
        t.interrupt();


        for (int i = 0; i < 5; i++) {
            log.debug("unpark");
            LockSupport.unpark(t);
            Sleeper.sleep(1);
        }
    }
}
