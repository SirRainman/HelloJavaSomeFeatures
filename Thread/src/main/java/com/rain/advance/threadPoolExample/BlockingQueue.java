package com.rain.advance.threadPoolExample;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: rain-java-ideas
 * @description: 阻塞队列
 * @author: Rain
 * @create: 2021-02-24 16:22
 **/
@Slf4j(topic = "c.BlockingQueue")
public class BlockingQueue<T> {
    // 任务队列
    Deque<T> queue = new ArrayDeque<>();
    // 队列容量
    int capacity;
    // 锁
    ReentrantLock lock = new ReentrantLock();
    // 生产者条件变量
    Condition full = lock.newCondition();
    // 消费者条件变量
    Condition empty = lock.newCondition();

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    // 阻塞存储
    public void put(T task) {
        lock.lock();
        try {
            while(queue.size() == capacity) {
                try {
                    log.info("{} is waiting to be put into blocking queue", task);
                    empty.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            queue.addLast(task);
            log.info("{} is put into blocking queue, now queue size={}", task, queue.size());
            full.signal();
        } finally {
            lock.unlock();
        }
    }

    // 阻塞队列已满，让调用者放弃任务执行
    public void tryPut(RejectPolicy<T> rejectPolicy, T task) {
        lock.lock();
        try {
            if(queue.size() == capacity) {
                log.error("Error: blocking queue is full");
                rejectPolicy.reject(this, task);
            } else {
                queue.addLast(task);
                log.info("{} is put into blocking queue, now queue size={}", task, queue.size());
                full.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    // 带超时阻塞存储
    public boolean offer(T task, long timeout, TimeUnit timeUnit) {
        lock.lock();
        try {
            // 将 timeout 统一转换为 纳秒
            long maxWaitTimeNanos = timeUnit.toNanos(timeout);
            while(queue.size() == capacity) {
                try {
                    if (maxWaitTimeNanos <= 0) return false;
                    log.info("{} is waiting to be put into blocking queue", task);
                    maxWaitTimeNanos = empty.awaitNanos(maxWaitTimeNanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            queue.addLast(task);
            log.info("{} is put into blocking queue, now queue size={}", task, queue.size());
            full.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    // 阻塞获取任务
    public T take() {
        lock.lock();
        try {
            while(queue.isEmpty()) {
                try {
                    full.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = queue.removeFirst();
            log.info("{} is removed from blocking queue, now queue size={}", t, queue.size());
            empty.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    // 带超时阻塞获取任务
    public T poll(long timeout, TimeUnit timeUnit) {
        lock.lock();
        try {
            // 将 timeout 统一转换为 纳秒
            long maxWaitTimeNanos = timeUnit.toNanos(timeout);
            while(queue.isEmpty()) {
                try {
                    if(maxWaitTimeNanos <= 0) return null;
                    maxWaitTimeNanos = full.awaitNanos(maxWaitTimeNanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = queue.removeFirst();
            log.info("{} is removed from blocking queue, now queue size={}", t, queue.size());
            empty.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }
}
