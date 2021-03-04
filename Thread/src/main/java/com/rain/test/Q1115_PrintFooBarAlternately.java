package com.rain.test;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: rain-java-ideas
 * @description: 两个不同的线程将会共用一个 FooBar实例。其中一个线程将会调用foo()方法，另一个线程将会调用bar()方法。
 * 请设计修改程序，以确保 "foobar" 被输出 n 次。
 * <p>
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/print-foobar-alternately
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 * @author: Rain
 * @create: 2021-03-03 19:34
 **/
public class Q1115_PrintFooBarAlternately {
    // TODO: 信号量 适合控制顺序
    class FooBar1 {
        private int n;
        private Semaphore s1, s2;

        public FooBar1(int n) {
            this.n = n;
            s1 = new Semaphore(1);
            s2 = new Semaphore(0);
        }

        public void foo(Runnable printFoo) throws InterruptedException {

            for (int i = 0; i < n; i++) {
                s1.acquire();
                // printFoo.run() outputs "foo". Do not change or remove this line.
                printFoo.run();
                s2.release();
            }
        }

        public void bar(Runnable printBar) throws InterruptedException {

            for (int i = 0; i < n; i++) {
                s2.acquire();
                // printBar.run() outputs "bar". Do not change or remove this line.
                printBar.run();
                s1.release();
            }
        }
    }


    // TODO: BLOCKING Queue
    public class FooBar2 {
        private int n;
        private BlockingQueue<Integer> bar = new LinkedBlockingQueue<>(1);
        private BlockingQueue<Integer> foo = new LinkedBlockingQueue<>(1);

        public FooBar2(int n) {
            this.n = n;
        }

        public void foo(Runnable printFoo) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                foo.put(i);
                printFoo.run();
                bar.put(i);
            }
        }

        public void bar(Runnable printBar) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                bar.take();
                printBar.run();
                foo.take();
            }
        }
    }

    // TODO: 可重入锁 + Condition
    class FooBar3 {
        private int n;

        public FooBar3(int n) {
            this.n = n;
        }

        Lock lock = new ReentrantLock(true);
        private final Condition foo = lock.newCondition();
        volatile boolean flag = true;

        public void foo(Runnable printFoo) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                lock.lock();
                try {
                    while (!flag) {
                        foo.await();
                    }
                    printFoo.run();
                    flag = false;
                    foo.signal();
                } finally {
                    lock.unlock();
                }
            }
        }

        public void bar(Runnable printBar) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                lock.lock();
                try {
                    while (flag) {
                        foo.await();
                    }
                    printBar.run();
                    flag = true;
                    foo.signal();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    // TODO: synchronized + 标志位 + wait/notify
    class FooBar4 {
        private int n;
        private volatile boolean flag = false;
        private Object lock = new Object();

        public FooBar4(int n) {
            this.n = n;
        }

        public void foo(Runnable printFoo) throws InterruptedException {

            for (int i = 0; i < n; i++) {
                synchronized (lock) {
                    if (flag) lock.wait();
                    // printFoo.run() outputs "foo". Do not change or remove this line.
                    printFoo.run();
                    flag = true;
                    lock.notify();
                }
            }
        }

        public void bar(Runnable printBar) throws InterruptedException {

            for (int i = 0; i < n; i++) {
                synchronized (lock) {
                    if (!flag) lock.wait();
                    // printBar.run() outputs "bar". Do not change or remove this line.
                    printBar.run();
                    flag = false;
                    lock.notify();
                }
            }
        }
    }

    //手阳明大肠经CyclicBarrier 控制先后
    class FooBar5 {
        private int n;

        public FooBar5(int n) {
            this.n = n;
        }

        CyclicBarrier cb = new CyclicBarrier(2);
        volatile boolean fin = true;

        public void foo(Runnable printFoo) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                while (!fin) ;
                printFoo.run();
                fin = false;
                try {
                    cb.await();
                } catch (BrokenBarrierException e) {
                }
            }
        }

        public void bar(Runnable printBar) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                try {
                    cb.await();
                } catch (BrokenBarrierException e) {
                }
                printBar.run();
                fin = true;
            }
        }
    }

    // 自旋 + 让出CPU
    class FooBar6 {
        private int n;

        public FooBar6(int n) {
            this.n = n;
        }

        volatile boolean flag = false;

        public void foo(Runnable printFoo) throws InterruptedException {

            for (int i = 0; i < n; i++) {
                while (flag) Thread.yield();
                // printFoo.run() outputs "foo". Do not change or remove this line.
                printFoo.run();
                flag = true;
            }
        }

        public void bar(Runnable printBar) throws InterruptedException {

            for (int i = 0; i < n; i++) {
                while (!flag) Thread.yield();
                // printBar.run() outputs "bar". Do not change or remove this line.
                printBar.run();
                flag = false;
            }
        }
    }


}
