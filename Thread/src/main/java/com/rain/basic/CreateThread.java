package com.rain.basic;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Slf4j(topic = "c.Basic")
public class CreateThread {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                log.debug("t1 running");
            }
        };
        t1.setName("t1");
        t1.start();

        Thread t2 = new Thread(() -> {
            log.debug("t2 running");
        }, "t2");
        t2.setName("t2");
        t2.start();

        Runnable runnable = () -> {
            log.debug("t3 running");
        };
        Thread t3 = new Thread(runnable, "t3");
        t3.start();

        // FutureTask 能够接收 Callable 类型的参数，用来处理有返回结果的情况
        FutureTask task4 = new FutureTask<>(() -> {
            log.debug("task4");
            return 100;
        });
        Thread t4 = new Thread(task4, "t4");
        t4.start();
        // 主线程阻塞，同步等待 task 执行完毕的结果
        log.debug("get result from t4 = {}", task4.get());

        log.debug("do other things ...");
    }
}
