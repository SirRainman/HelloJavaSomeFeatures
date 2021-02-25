package com.rain.advance;

import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.rain.util.Sleeper.sleep;

/**
 * @program: rain-java-ideas
 * @description: 一些常见的线程池
 * @author: Rain
 * @create: 2021-02-24 19:46
 **/
@Slf4j(topic = "c.ThreadPoolTypes")
public class ThreadPoolTypes {
    public static void main(String[] args) {
        // testFixed();

        // testCached();

        testSingle();
    }

    public static void testFixed() {
        // ExecutorService threadPool = Executors.newFixedThreadPool(2);
        // TODO：注意是怎么命名的
        ExecutorService threadPool = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private AtomicInteger threadCount = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "myPool_t_" + threadCount.getAndIncrement());
            }
        });
        for (int i = 0; i < 3; i++) {
            int index = i;
            threadPool.execute(() -> {
                log.info("{}", index);
            });
        }
    }

    public static void testCached() {
        // TODO: 仔细的体会，不取不能放的特点
        SynchronousQueue<Integer> integers = new SynchronousQueue<>();
        new Thread(() -> {
            try {
                log.debug("putting {} ", 1);
                integers.put(1);
                log.debug("{} putted...", 1);

                log.debug("putting...{} ", 2);
                integers.put(2);
                log.debug("{} putted...", 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t1").start();

        sleep(1);

        new Thread(() -> {
            try {
                log.debug("taking {}", integers.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t2").start();

        sleep(1);

        new Thread(() -> {
            try {
                log.debug("taking {}", integers.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t3").start();
    }

    public static void testSingle() {
        ExecutorService threadPool = Executors.newSingleThreadExecutor();

        threadPool.execute(() -> {
            log.info("1");
        });

        threadPool.execute(() -> {
            log.info("2");
            int i = 1 / 0;
        });

        threadPool.execute(() -> {
            log.info("3");
        });
    }

    /**
     * Timer 的优点在于简单易用，但由于所有任务都是由同一个线程来调度，因此所有任务都是串行执行的，
     * 同一时间只能有一个任务在执行，前一个任务的延迟或异常都将会影响到之后的任务。
     * 其中一个线程有错误也会耽误其他的线程
     */
    public static void testTimer() {
        Timer timer = new Timer();
        TimerTask task1 = new TimerTask() {
            @Override
            public void run() {
                log.debug("task 1");
                sleep(2);
            }
        };
        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                log.debug("task 2");
            }
        };
        // 使用 timer 添加两个任务，希望它们都在 1s 后执行
        // 但由于 timer 内只有一个线程来顺序执行队列中的任务，因此『任务1』的延时，影响了『任务2』的执行
        timer.schedule(task1, 1000);
        timer.schedule(task2, 1000);
    }

    /**
     * 整个线程池表现为：
     * 线程数固定，任务数多于线程数时，会放入无界队列排队。
     * 任务执行完毕，这些线程也不会被释放, 用来执行延迟或反复执行的任务
     */
    public static void testScheduled() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        // 添加两个任务，希望它们都在 1s 后执行
        executor.schedule(() -> {
            System.out.println("任务1，执行时间：" + new Date());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }, 1000, TimeUnit.MILLISECONDS);

        executor.schedule(() -> {
            System.out.println("任务2，执行时间：" + new Date());
        }, 1000, TimeUnit.MILLISECONDS);
    }

    public static void testScheduled2() {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        log.debug("start...");
        // pool.scheduleAtFixedRate(() -> {
        //     log.debug("running...");
        // }, 1, 1, TimeUnit.SECONDS);

        // 一开始，延时 1s，接下来，由于任务执行时间 > 间隔时间，间隔被『撑』到了 2s
        pool.scheduleAtFixedRate(() -> {
            log.debug("running...");
            sleep(2);
        }, 1, 1, TimeUnit.SECONDS);

        // 一开始，延时 1s，scheduleWithFixedDelay 的间隔是 上一个任务结束 <-> 延时 <-> 下一个任务开始 所以间隔都是 3s
        pool.scheduleWithFixedDelay(() -> {
            log.debug("running...");
            sleep(2);
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static void testScheduled3() {
        // 获得当前时间
        LocalDateTime now = LocalDateTime.now();
        // 获取本周四 18:00:00.000
        LocalDateTime thursday =
                now.with(DayOfWeek.THURSDAY).withHour(18).withMinute(0).withSecond(0).withNano(0);
        // 如果当前时间已经超过 本周四 18:00:00.000， 那么找下周四 18:00:00.000
        if (now.compareTo(thursday) >= 0) {
            thursday = thursday.plusWeeks(1);
        }
        // 计算时间差，即延时执行时间
        long initialDelay = Duration.between(now, thursday).toMillis();
        // 计算间隔时间，即 1 周的毫秒值
        long oneWeek = 7 * 24 * 3600 * 1000;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    }

    public static void testException() throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(1);

        // 方法1：主动捉异常
        pool.submit(() -> {
            try {
                log.debug("task1");
                int i = 1 / 0;
            } catch (Exception e) {
                log.error("error:", e);
            }
        });

        // 方法2：使用 Future
        Future<Boolean> f = pool.submit(() -> {
            log.debug("task1");
            int i = 1 / 0;
            return true;
        });
        log.debug("result:{}", f.get());
    }
}
