package com.rain.advance.threadPoolExample;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-02-24 15:25
 **/
@Slf4j(topic = "c.ThreadPoolTest")
public class ThreadPoolTest {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(1, 1000, TimeUnit.MILLISECONDS, 1, (queue, task) -> {
            // 1. 死等
            // queue.put(task);

            // 2) 带超时等待
            // queue.offer(task, 1500, TimeUnit.MILLISECONDS);

            // 3) 让调用者放弃任务执行
            // log.debug("放弃{}", task);

            // 4) 让调用者抛出异常
            // throw new RuntimeException("任务执行失败 " + task);

            // 5) 让调用者自己执行任务
            task.run();
        });

        for(int i = 0; i < 5; i++) {
            Task task = new Task(i);
            threadPool.execute(task);
        }
    }
}
