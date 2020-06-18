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
        /**
         * 默认情况下，Java进程需要等待所有线程都运行结束，才会结束。
         * 守护线程：只要其它非守护线程运行结束了，即使守护线程的代码没有执行完，也会强制结束。
         * */
        t.setDaemon(true);
        t.start();

        Sleeper.sleep(1);
        log.debug("main end");
    }
}
