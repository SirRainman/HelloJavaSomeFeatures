package com.rain.basic;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-02-18 17:27
 **/
@Slf4j
public class WaitNotify {
    static boolean flag = true;
    static Object lock = new Object();

    @Test
    public void testWaitNotify() throws InterruptedException {
        Thread waitThread = new Thread(new Wait(), "WaitThread");
        waitThread.start();

        Sleeper.sleep(1);

        Thread notifyThread = new Thread(new Notify(), "NotifyThread");
        notifyThread.start();

        waitThread.join();
        notifyThread.join();
    }

    static class Wait implements Runnable {

        public void run() {
            // 加锁，拥有lock的Monitor
            synchronized (lock) {
                // 当条件不满足时，继续wait，同时释放了lock的锁
                while (flag) {
                    try {
                        log.info("flag is true");
                        lock.wait();
                    } catch (InterruptedException e) {

                    }
                }
                // 条件满足时，完成工作
                log.info("flag is false");
            }
        }
    }

    static class Notify implements Runnable {
        public void run() {
            // 加锁，拥有lock的Monitor
            synchronized (lock) {
                // 获取lock的锁，然后进行通知，通知时不会释放lock的锁，
                // 直到当前线程释放了lock后，WaitThread才能从wait方法中返回
                log.info("hold lock, notify()");
                lock.notifyAll();
                flag = false;
                Sleeper.sleep(5);
            }
            //// 再次加锁
            //synchronized (lock) {
            //    log.info("hold lock again");
            //    Sleeper.sleep(5);
            //}
        }
    }

    static class PrintNumberTask implements Runnable {
        private byte[] lock;

        private static int x = 1;

        public PrintNumberTask(byte[] lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            while(x < 10) {
                synchronized (lock) {
                    log.info("{}", x++);
                    lock.notify();
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        byte[] lock = new byte[0];
        new Thread(new PrintNumberTask(lock), "t1").start();
        new Thread(new PrintNumberTask(lock), "t2").start();
        Sleeper.sleep(1);
        lock.notify();
    }
}