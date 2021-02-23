package com.rain.basic;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

@Slf4j(topic = "c.Interrupt")
public class Interrupt {

    @Test
    public void basicUse() {
        Thread t = new Thread(() -> {
            while(true) {
                if (Thread.currentThread().isInterrupted()) {
                    log.info("t is interrupted, exit now");
                    break;
                }
            }
        }, "t");
        t.start();

        Sleeper.sleep(1);
        log.info("begin interrupt()");
        t.interrupt();
        log.info("after interrupt()");
    }

    @Test
    public void InterruptWhileSleeping() throws InterruptedException {
        Thread t = new Thread(()->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.info("error: {}", e.getMessage());
            }
        }, "t1");
        t.start();

        Sleeper.sleep(1);
        // 不能在线程睡眠的时候打断，否则会抛出 InterruptedException 异常
        t.interrupt();
        log.info("t's isInterrupted={}", t.isInterrupted());
    }

    @Test
    public void testTwoPhaseTermination () {
        TwoPhaseTermination twoPhaseTermination = new TwoPhaseTermination();

        twoPhaseTermination.start();

        Sleeper.sleep(3.5);

        log.info("end monitoring");
        twoPhaseTermination.stop();
    }

    class TwoPhaseTermination {
        private Thread monitorThread;

        public void start() {
            monitorThread = new Thread(() -> {
                while (true) {
                    Thread currentThread = Thread.currentThread();
                    if (currentThread.isInterrupted()) {
                        log.info("monitor thread is interrupted, do something to end");
                        break;
                    }

                    try {
                        Thread.sleep(1000);
                        log.info("nothing, just save result");
                    } catch (InterruptedException e) { // current thread is interrupted while sleeping
                        // 在睡眠时出现异常后，会清楚打断标记，需要重置打断标记
                        log.info("{}", e);
                        currentThread.interrupt();
                    }
                }
            }, "MonitorThread");
            monitorThread.start();
        }

        public void stop() {
            monitorThread.interrupt();
        }
    }

    class TwoPhaseTermination2 {
        // 监控线程
        private Thread monitorThread;
        // 停止标记, 停止标记用 volatile 是为了保证该变量在多个线程之间的可见性
        private volatile boolean stop = false;
        // 判断是否执行过start方法
        private volatile boolean starting = false;

        public void start() {
            System.out.println();
            synchronized (this) { // 防止多个线程同时读到start = false，还是会重复创建monitor线程，加锁进行保护
                if (this.starting) { // balking 模式，防止多次调用start方法多次创建monitor线程
                    return;
                }
                starting = true;
            }

            this.monitorThread = new Thread(() -> {
                while(true) {
                    if (this.stop) {
                        log.info("end:do somethings");
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                        log.info("monitor...");
                    } catch (InterruptedException e) {
                        log.info("---interrupt while sleeping---");
                        // e.printStackTrace();
                    }
                }
            }, "monitorThread");
            this.monitorThread.start();
        }

        public void stop() {
            log.info("stop two phase termination");
            this.stop = true;
            this.monitorThread.interrupt();
        }

    }
}