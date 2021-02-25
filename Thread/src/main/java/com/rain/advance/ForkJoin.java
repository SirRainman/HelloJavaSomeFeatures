package com.rain.advance;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-02-25 10:42
 **/
public class ForkJoin {
    public static void main(String[] args) {
        // TODO: 想一想只启动一个线程去执行会不会饥饿？
        //  为什么不会饥饿？
        ForkJoinPool pool = new ForkJoinPool(2);
        System.out.println(pool.invoke(new AddTask2(1, 4)));
    }
}

@Slf4j
class AddTask2 extends RecursiveTask<Integer> {
    int begin, end;

    public AddTask2(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        if(begin == end) return begin;

        int mid = begin + end >> 1;
        AddTask2 t1 = new AddTask2(begin, mid);
        t1.fork();
        AddTask2 t2 = new AddTask2(mid + 1, end);
        t2.fork();
        log.debug("fork() {} + {} = ?", t1, t2);

        int res = t1.join() + t2.join();
        log.debug("join() {} + {} = {}", t1, t2, res);
        return res;
    }

    @Override
    public String toString() {
        return "{" + begin + ", " + end + "}";
    }
}

@Slf4j
class AddTask1 extends RecursiveTask<Integer> {
    int n;

    @Override
    public String toString() {
        return "{" + n + "}";
    }

    @Override
    protected Integer compute() {
        if(n == 1) return 1;

        AddTask1 t1 = new AddTask1(n - 1);
        t1.fork();

        log.debug("fork() {} + {}", n, t1);
        // 合并(join)结果
        int result = n + t1.join();
        log.debug("join() {} + {} = {}", n, t1, result);
        return result;
    }

    public AddTask1(int n) {
        this.n = n;
    }
}