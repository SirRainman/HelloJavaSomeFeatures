package com.rain.advance;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Program: rain-java-ideas
 * @Description: 实现类似于CountdownLatch相同的功能
 * @Author: HouHao Ye
 * @Create: 2021-02-27 21:27
 **/
@Slf4j
public class CyclicBarrierExample {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2, () -> {
            log.info("---------task1 task2 end-------");
        }); // 个数为2时才会继续执行, 最后的函数是结束时执行
        for (int i = 0; i < 3; i++) {
            executorService.submit( () -> {
                log.info("线程1开始..");
                try {
                    cyclicBarrier.await(); // 当个数不足时，等待
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });

            executorService.submit( ()->{
                log.info("线程2开始..");
                try { Thread.sleep(2000); } catch (InterruptedException e) { }
                try {
                    cyclicBarrier.await(); // 2 秒后，线程个数够2，继续运行
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
        }
        executorService.shutdown();
    }
}
