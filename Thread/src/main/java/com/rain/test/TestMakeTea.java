package com.rain.test;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.TestMakeTea")
public class TestMakeTea {


    public static void main(String[] args) {
        S3.makeTea();
    }

    private static void s1() {
        Thread t1 = new Thread(() -> {
            log.info("洗水壶");
            Sleeper.sleep(1);
            log.info("烧开水");
            Sleeper.sleep(15);
        }, "老王");

        Thread t2 = new Thread(() -> {
            log.info("洗茶壶");
            Sleeper.sleep(1);
            log.info("洗茶杯");
            Sleeper.sleep(2);
            log.info("拿茶叶");
            Sleeper.sleep(1);
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("泡茶");
        }, "小王");

        t1.start();
        t2.start();
    }
}

@Slf4j(topic = "c.S2")
class S2 {
    static String kettle = "冷水";
    static String tea = null;
    static final Object lock = new Object();
    static boolean maked = false;

    public static void makeTea() {
        new Thread(() -> {
            log.info("洗水壶");
            Sleeper.sleep(1);
            log.info("烧开水");
            Sleeper.sleep(5);
            synchronized (lock) {
                kettle = "开水";
                lock.notifyAll();
                while (tea == null) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!maked) {
                    log.info("拿({})泡({})", kettle, tea);
                    maked = true;
                }
            }
        }, "老王").start();

        new Thread(() -> {
            log.info("洗茶壶");
            Sleeper.sleep(1);
            log.info("洗茶杯");
            Sleeper.sleep(2);
            log.info("拿茶叶");
            Sleeper.sleep(1);
            synchronized (lock) {
                tea = "花茶";
                lock.notifyAll();
                while (kettle.equals("冷水")) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!maked) {
                    log.info("拿({})泡({})", kettle, tea);
                    maked = true;
                }
            }
        }, "小王").start();
    }
}

@Slf4j(topic = "c.S3")
class S3 {
    static String kettle = "冷水";
    static String tea = null;
    static final Object lock = new Object();

    public static void makeTea() {
        new Thread(() -> {
            log.info("洗水壶");
            Sleeper.sleep(1);
            log.info("烧开水");
            Sleeper.sleep(5);
            synchronized (lock) {
                kettle = "开水";
                lock.notifyAll();
            }
        }, "老王").start();

        new Thread(() -> {
            log.info("洗茶壶");
            Sleeper.sleep(1);
            log.info("洗茶杯");
            Sleeper.sleep(2);
            log.info("拿茶叶");
            Sleeper.sleep(1);
            synchronized (lock) {
                tea = "花茶";
                lock.notifyAll();
            }
        }, "小王").start();

        new Thread(() -> {
            synchronized (lock) {
                while (kettle.equals("冷水") || tea == null) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.info("拿({})泡({})", kettle, tea);
            }
        }, "王夫人").start();

    }
}
