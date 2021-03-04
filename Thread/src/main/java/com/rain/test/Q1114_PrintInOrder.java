package com.rain.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: rain-java-ideas
 * @description:
 * 执行顺序
 * first() -> second() -> third()
 * @author: Rain
 * @create: 2021-03-03 18:45
 **/
public class Q1114_PrintInOrder {
    // TODO: 锁
    //  首先，这道题是要解决线程按序打印问题。
    //  我们可以确定的是只有3个线程，3个方法，以及方法执行的先后次序；而不确定的是哪个线程会先执行（这是并发问题）。
    //  所有的解法都是一个理念,无论采用何种方式，
    //      开始时必须执行first线程，
    //      然后设置条件满足second执行, 而first和third线程都不能执行，
    //      同时只有first线程执行完才能给与该条件，
    //      然后设置条件满足third执行而first和second线程都不能执行，
    //      同时只有second线程执行成功后才能给与该条件
    static class Foo {
        // TODO: 为什么要设置 num？
        //  如果不用num，一旦先执行了signal操作，signal信号会丢失，一旦丢失就陷入无限等待，
        //  用num来保证第一个执行的是first()，同时也是精准通知和唤醒线程时的判断条件
        //  而信号量（semaphore）的signal不会丢失
        int num;
        Lock lock;
        //精确的通知和唤醒线程
        Condition c1, c2, c3;

        public Foo() {
            num = 1;
            lock = new ReentrantLock();
            c1 = lock.newCondition();
            c2 = lock.newCondition();
            c3 = lock.newCondition();
        }

        public void first(Runnable printFirst) throws InterruptedException {
            lock.lock();
            try {
                while (num != 1) {
                    c1.await();
                }
                num = 2;
                // printFirst.run() outputs "first". Do not change or remove this line.
                printFirst.run();
                c2.signal();
            } finally {
                lock.unlock();
            }
        }

        public void second(Runnable printSecond) throws InterruptedException {
            lock.lock();
            try {
                while (num != 2) {
                    c2.await();
                }
                num = 3;
                // printSecond.run() outputs "second". Do not change or remove this line.
                printSecond.run();
                c3.signal();
            } finally {
                lock.unlock();
            }
        }

        public void third(Runnable printThird) throws InterruptedException {
            lock.lock();

            try {
                while (num != 3) {
                    c3.await();
                }
                num = 1;
                // printThird.run() outputs "third". Do not change or remove this line.
                printThird.run();
                c1.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    // TODO: 信号量
    //  重要
    //  Semaphore与CountDownLatch相似，不同的地方在于Semaphore的值被获取到后是可以释放的，并不像CountDownLatch那样一直减到底
    //  获得Semaphore的线程处理完它的逻辑之后，你就可以调用它的Release()函数将它的计数器重新加1，这样其它被阻塞的线程就可以得到调用了
    static class Foo2 {

        // 初始的允许请求均设为0
        private Semaphore s12, s23;

        public Foo2() {
            s12 = new Semaphore(0);
            s23 = new Semaphore(0);
        }

        public void first(Runnable printFirst) throws InterruptedException {

            // printFirst.run() outputs "first". Do not change or remove this line.
            printFirst.run();
            //释放一个12的信号量
            s12.release();
        }

        public void second(Runnable printSecond) throws InterruptedException {
            //获取一个12的信号量，没有则阻塞
            s12.acquire();
            // printSecond.run() outputs "second". Do not change or remove this line.
            printSecond.run();
            //释放一个23的信号量
            s23.release();
        }

        public void third(Runnable printThird) throws InterruptedException {
            //获取一个23的信号量，没有则阻塞
            s23.acquire();
            // printThird.run() outputs "third". Do not change or remove this line.
            printThird.run();
        }
    }

    // TODO: CountDownLatch
    class Foo3 {
        private CountDownLatch c12, c23;

        public Foo3() {
            c12 = new CountDownLatch(1);
            c23 = new CountDownLatch(1);
        }

        public void first(Runnable printFirst) throws InterruptedException {

            // printFirst.run() outputs "first". Do not change or remove this line.
            printFirst.run();
            c12.countDown();
        }

        public void second(Runnable printSecond) throws InterruptedException {
            c12.await();
            // printSecond.run() outputs "second". Do not change or remove this line.
            printSecond.run();
            c23.countDown();
        }

        public void third(Runnable printThird) throws InterruptedException {
            c23.await();
            // printThird.run() outputs "third". Do not change or remove this line.
            printThird.run();
        }
    }

    // TODO: BlockingQueue
    //  这个还不会
    static class Foo4 {
        BlockingQueue<String> blockingQueue12, blockingQueue23;

        public Foo4() {
            //同步队列,没有容量，进去一个元素，必须等待取出来以后，才能再往里面放一个元素
            blockingQueue12 = new SynchronousQueue<>();
            blockingQueue23 = new SynchronousQueue<>();
        }

        public void first(Runnable printFirst) throws InterruptedException {

            // printFirst.run() outputs "first". Do not change or remove this line.
            printFirst.run();
            blockingQueue12.put("stop");
        }

        public void second(Runnable printSecond) throws InterruptedException {
            blockingQueue12.take();
            // printSecond.run() outputs "second". Do not change or remove this line.
            printSecond.run();
            blockingQueue23.put("stop");
        }

        public void third(Runnable printThird) throws InterruptedException {
            blockingQueue23.take();
            // printThird.run() outputs "third". Do not change or remove this line.
            printThird.run();
        }
    }

    // TODO: volatile
    static class Foo5 {

        private volatile boolean firstJobDone = false;
        private volatile boolean secondJobDone = false;

        public Foo5() {

        }

        public void first(Runnable printFirst) throws InterruptedException {

            // printFirst.run() outputs "first". Do not change or remove this line.
            printFirst.run();
            firstJobDone = true;
        }

        public void second(Runnable printSecond) throws InterruptedException {

            while (!firstJobDone) {
                //等待printFirst完成
            }

            // printSecond.run() outputs "second". Do not change or remove this line.
            printSecond.run();

            secondJobDone = true;
        }

        public void third(Runnable printThird) throws InterruptedException {
            while (!secondJobDone) {
                //等待printSecond完成
            }

            // printThird.run() outputs "third". Do not change or remove this line.
            printThird.run();
        }
    }

    // TODO: AtomicInteger
    static class Foo6 {

        private AtomicInteger firstJobDone = new AtomicInteger(0);
        private AtomicInteger secondJobDone = new AtomicInteger(0);

        public Foo6() {}

        public void first(Runnable printFirst) throws InterruptedException {
            // printFirst.run() outputs "first".
            printFirst.run();
            // mark the first job as done, by increasing its count.
            firstJobDone.incrementAndGet();
        }

        public void second(Runnable printSecond) throws InterruptedException {
            while (firstJobDone.get() != 1) {
                // waiting for the first job to be done.
            }
            // printSecond.run() outputs "second".
            printSecond.run();
            // mark the second as done, by increasing its count.
            secondJobDone.incrementAndGet();
        }

        public void third(Runnable printThird) throws InterruptedException {
            while (secondJobDone.get() != 1) {
                // waiting for the second job to be done.
            }
            // printThird.run() outputs "third".
            printThird.run();
        }
    }

}
