package com.rain.basic;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Daemon")
public class Daemon {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            log.debug("t begin");
            while (true) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
            log.debug("t end, and isInterrupted={}", Thread.currentThread().isInterrupted());
        }, "t");
        t.setDaemon(true);
        t.start();

        Sleeper.sleep(1);
        log.debug("main end");
    }
}
