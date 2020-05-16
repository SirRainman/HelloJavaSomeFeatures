package com.rain.basic;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Interrupt")
public class Interrupt {

    public static void basicUse() {
        Thread t = new Thread(() -> {
            while(true) {
                if (Thread.currentThread().isInterrupted()) {
                    log.debug("t is interrupted, exit now");
                    break;
                }
            }
        }, "t");
        t.start();

        Sleeper.sleep(1);
        log.debug("begin interrupt");
        t.interrupt();
    }

    public static void main(String[] args) throws InterruptedException {
//        basicUse();
        Thread t = new Thread(()->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.debug("error: interrupt while sleep");
            }
        }, "t1");
        t.start();

        Sleeper.sleep(1);
        // 不能在线程睡眠的时候打断
        t.interrupt();
        log.debug("t's isInterrupted={}", t.isInterrupted());

//        testTwoPhaseTermination();
    }

    public static void testTwoPhaseTermination () {
        TwoPhaseTermination twoPhaseTermination = new TwoPhaseTermination();

        twoPhaseTermination.start();

        Sleeper.sleep(3.5);
        log.debug("end monitoring");
        twoPhaseTermination.stop();
    }

}

@Slf4j(topic = "c.TwoPhaseTermination")
class TwoPhaseTermination {
    // 监控线程
    private Thread monitorThread;
    // 停止标记
    private volatile boolean stop = false;
    // 判断是否执行过start方法
    private boolean starting = false;

    public void start() {
        synchronized (this) {
            if (this.starting) {
                return;
            }
            starting = true;
        }

        this.monitorThread = new Thread(() -> {
            while(true) {
                if (this.stop) {
                    log.debug("end:do somethings");
                    break;
                }
                try {
                    Thread.sleep(1000);
                    log.debug("monitor...");
                } catch (InterruptedException e) {
                    log.debug("---interrupt while sleeping---");
                    // e.printStackTrace();
                }
            }
        }, "monitorThread");
        this.monitorThread.start();
    }

    public void stop() {
        log.debug("stop two phase termination");
        this.stop = true;
        this.monitorThread.interrupt();
    }
}