package com.rain.advance;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Program: rain-java-ideas
 * @Description:
 * @Author: HouHao Ye
 * @Create: 2021-02-22 11:05
 **/
@Slf4j
public class Synchronize {
    private static int counter = 0;

    public void add() {
        synchronized (this) {
            for (int i = 0; i < 100; i++) {
                System.out.print(i + " ");
            }
        }
    }


    @Test
    public void testSynchronize() throws InterruptedException {
        Synchronize e1 = new Synchronize();
        Synchronize e2 = new Synchronize();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> e1.add());
        executorService.execute(() -> e2.add());
    }

    
}
