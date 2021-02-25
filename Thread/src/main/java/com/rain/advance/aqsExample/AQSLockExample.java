package com.rain.advance.aqsExample;

import lombok.extern.slf4j.Slf4j;

import static com.rain.util.Sleeper.sleep;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-02-25 13:37
 **/
@Slf4j
public class AQSLockExample {
    public static void main(String[] args) {
        MyLock lock = new MyLock();
        new Thread(() -> {
            lock.lock();
            lock.lock();
            try {
                log.debug("locking...");
                sleep(1);
            } finally {
                log.debug("unlocking...");
                lock.unlock();
            }
        },"t1").start();
        new Thread(() -> {
            lock.lock();
            try {
                log.debug("locking...");
            } finally {
                log.debug("unlocking...");
                lock.unlock();
            }
        },"t2").start();
    }
}
