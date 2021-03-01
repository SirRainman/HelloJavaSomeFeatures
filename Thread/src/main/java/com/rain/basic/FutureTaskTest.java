package com.rain.basic;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @Program: rain-java-ideas
 * @Description:
 * @Author: HouHao Ye
 * @Create: 2021-02-28 20:29
 **/
@Slf4j
public class FutureTaskTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        MyCallable myCallable = new MyCallable();
        FutureTask task1 = new FutureTask<>(myCallable);
        FutureTask task2 = new FutureTask<>(myCallable);
        Thread t1 = new Thread(task1, "t1");
        Thread t2 = new Thread(task2, "t2");

        t1.start();
        log.info("{}", task1.get());

        Sleeper.sleep(1);
        t2.start();
    }

    static class MyCallable implements Callable {
        @Override
        public Integer call() throws Exception {
            log.info("executing...");
            return 100;
        }
    }
}
