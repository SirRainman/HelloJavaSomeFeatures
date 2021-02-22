package com.rain.basic;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-02-21 21:43
 **/
@Slf4j
public class DeadLock {
    private static Object lock1 = new Object();
    private static Object lock2 = new Object();

    @Test
    public void createDeadLock() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                log.info("{} get lock1", Thread.currentThread());
                Sleeper.sleep(1);
                log.info("{} waiting for lock2", Thread.currentThread());
                synchronized (lock2) {
                    log.info("{} get lock2", Thread.currentThread());
                }
            }
        }, "t1");

        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                log.info("{} get lock2", Thread.currentThread());
                Sleeper.sleep(1);
                log.info("{} waiting for lock1", Thread.currentThread());
                synchronized (lock1) {
                    log.info("{} get lock1", Thread.currentThread());
                }
            }
        }, "t2");

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }
}
