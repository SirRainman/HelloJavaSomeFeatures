package com.rain.advance.threadPoolExample;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * @program: rain-java-ideas
 * @description: 线程池，管理线程
 * @author: Rain
 * @create: 2021-02-24 16:33
 **/
@Slf4j(topic = "c.ThreadPool")
public class ThreadPool {
    // 核心线程数
    private int corePoolSize;
    // 线程集合
    private HashSet<Worker> workers = new HashSet<>();
    // 任务阻塞队列
    private BlockingQueue<Runnable> taskQueue;
    // 任务超时时间
    private long timeout;
    private TimeUnit timeUnit;
    // 拒绝策略
    private RejectPolicy<Runnable> rejectPolicy;

    public ThreadPool(int corePoolSize, long timeout, TimeUnit timeUnit, int capacity, RejectPolicy<Runnable> rejectPolicy) {
        this.corePoolSize = corePoolSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.taskQueue = new BlockingQueue<>(capacity);
        this.rejectPolicy = rejectPolicy;
    }

    // 执行任务
    public void execute(Runnable task) {
        synchronized (workers) { // 防止多个线程同时执行execute()函数时，同时读到workers
            if (workers.size() < corePoolSize) { // 当任务数没有超过 coreSize 时，直接交给 worker 对象执行
                log.info("thread number is {}, create new worker to execute task={}", workers.size(), task);
                Worker worker = new Worker(task);
                workers.add(worker);
                worker.start();
            } else { // 线程数目以超过corePoolSize，将任务放到阻塞队列中
                log.info("can't create worker for {}", task);
                // 拒绝策略
                // 1) 死等
                // 2) 带超时等待
                // 3) 让调用者放弃任务执行
                // 4) 让调用者抛出异常
                // 5) 让调用者自己执行任务
                taskQueue.tryPut(rejectPolicy, task);
            }
        }
    }

    class Worker extends Thread {
        Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            // 执行任务
            // 1) 当 task 不为空，执行任务
            // 2) 当 task 执行完毕，再接着从任务队列获取任务并执行
            // while(task != null || (task = taskQueue.take()) != null) {
            while (task != null || (task = taskQueue.poll(timeout, timeUnit)) != null) {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    task = null;
                }
            }
            synchronized (workers) {
                log.info("worker={} is destroyed", this.getName());
                workers.remove(this);
            }
        }
    }
}
