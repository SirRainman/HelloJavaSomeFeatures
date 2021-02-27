package com.rain.advance;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Program: rain-java-ideas
 * @Description: 使用Semaphore实现生产者和消费者
 * @Author: HouHao Ye
 * @Create: 2021-02-27 20:19
 **/
public class SemaphoreExample {
    class SemaphoreTest {
        private int n = 100;
        private Semaphore mutex = new Semaphore(1);
        private Semaphore notFull = new Semaphore(n);
        private Semaphore notEmpty = new Semaphore(0);

        public void product() throws InterruptedException {
            notFull.acquire(); // 缓冲区满时，生产者线程必须等待
            mutex.acquire();
            // ...
            mutex.release();
            notEmpty.release(); // 唤醒等待的消费者线程
        }

        public void consume() throws InterruptedException {
            notEmpty.acquire(); // 缓冲区空时，消费都线程等待
            mutex.acquire();
            // ...
            mutex.release();
            notFull.release(); // 唤醒等待的生产者线程
        }
    }

    class ConditionTest<T> {
        final Lock lock = new ReentrantLock();
        final Condition notFull = lock.newCondition();
        final Condition notEmpty = lock.newCondition();

        private boolean isFull;
        private boolean isEmpty;

        void enq(T x) throws InterruptedException {
            lock.lock();
            try {
                while (isFull == true) { // 队列已满
                    notFull.await();
                }
                //入队后，通知可出队
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        void deq() throws InterruptedException {
            lock.lock();
            try {
                while (isEmpty == true) { // 队列空
                    notEmpty.await();
                }
                // 出队后，通知可入队
                notFull.signal();
            } finally {
                lock.unlock();
            }

        }
    }
}
