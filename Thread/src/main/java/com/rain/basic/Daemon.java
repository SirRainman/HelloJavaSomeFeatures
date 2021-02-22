package com.rain.basic;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Daemon {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            log.info("t begin");
            while (true) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
            log.info("t end, and isInterrupted={}", Thread.currentThread().isInterrupted());
        }, "t");
        /**
         * 守护线程是：程序运行时在后台提供服务的线程，不属于程序中不可或缺的部分。
         * 默认情况下，Java进程需要等待所有线程都运行结束，才会结束。
         * 守护线程：只要其它非守护线程运行结束了，即使守护线程的代码没有执行完，也会被强制结束。
         * */
        t.setDaemon(true); // 如果不设置为守护进程，main进程作为非守护进程，会一直等待t执行结束
        t.start();

        Sleeper.sleep(1);
        log.info("main end");
    }
}
