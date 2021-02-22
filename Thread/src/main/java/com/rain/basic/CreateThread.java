package com.rain.basic;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Slf4j
public class CreateThread {

    @Test
    public void threadTypes() {
        // 获取 Java 线程管理 MXBean
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        // 不需要获取同步的 monitor 和 synchronizer 信息，仅获取线程和线程堆栈信息
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
        // 遍历线程信息，仅打印线程 ID 和线程名称信息
        for (ThreadInfo threadInfo : threadInfos) {
            log.info("[{}] {}", threadInfo.getThreadId(), threadInfo.getThreadName());
        }
    }

    @Test
    public void creat() throws ExecutionException, InterruptedException {
        // 1 继承Thread类
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                log.info("t1 running");
            }
        };
        // MyThread mt = new MyThread(); // MyThread类 继承Thread类
        t1.setName("t1");
        t1.start();

        // 2 实现Runnable接口：推荐
        Thread t2 = new Thread(() -> {
            log.info("t2 running");
        }, "t2");
        t2.setName("t2");
        t2.start();
        // 注：2 等同于上一种方式， MyRunnable类 实现了Runnable接口
        // MyRunnable instance = new MyRunnable();
        // Thread t2 = new Thread(instance, "t2");

        // 注：2 实现Runnable接口
        Runnable runnable = () -> {
            log.info("t3 running");
        };
        Thread t3 = new Thread(runnable, "t3");
        t3.start();


        // 3 实现Callable接口：FutureTask 能够接收 Callable 类型的参数，用来处理有返回结果的情况
        FutureTask task4 = new FutureTask<>(() -> {
            log.info("task4");
            return 100;
        });
        Thread t4 = new Thread(task4, "t4");

        // 等同于上面的方式，MyCallable类 实现了Callable接口
        // MyCallable mc = new MyCallable();
        // FutureTask<Integer> ft = new FutureTask<>(mc);
        // Thread task4 = new Thread(ft);

        t4.start();
        // 主线程阻塞，同步等待 task 执行完毕的结果
        log.info("get result from t4 = {}", task4.get());

        log.info("do other things ...");
    }
}
