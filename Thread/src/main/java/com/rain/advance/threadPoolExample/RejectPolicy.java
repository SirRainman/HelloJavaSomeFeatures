package com.rain.advance.threadPoolExample;

/**
 * @program: rain-java-ideas
 * @description: 当活跃线程数达到上限，且阻塞队列满时，通过拒绝策略拒绝处理任务
 * @author: Rain
 * @create: 2021-02-24 16:15
 **/
@FunctionalInterface
public interface RejectPolicy<T> {
    void reject(BlockingQueue<T> queue, T task);
}
