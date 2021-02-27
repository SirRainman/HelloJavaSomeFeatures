package com.rain.advance;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Program: rain-java-ideas
 * @Description:
 * @Author: HouHao Ye
 * @Create: 2021-02-27 18:19
 **/
public class ReadWriteLock {
    class CachedData {
        Object data;
        // 是否有效，如果失效，需要重新计算 data
        volatile boolean cacheValid;
        final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

        void processCachedData() {
            rwl.readLock().lock();
            if (!cacheValid) {
                // 获取写锁前必须释放读锁
                rwl.readLock().unlock();
                // 释放完读锁，才能释放写锁
                rwl.writeLock().lock();
                try {
                    // 判断是否有其它线程已经获取了写锁、更新了缓存, 避免重复更新
                    if (!cacheValid) {
                        // data = ...;
                        cacheValid = true;
                    }
                    // 获取写锁后，可以获取读锁
                    // 降级为读锁, 释放写锁, 这样能够让其它线程读取缓存
                    rwl.readLock().lock();
                } finally {
                    rwl.writeLock().unlock();
                }
            }
            // 自己用完数据, 释放读锁
            try {
                // use(data);
            } finally {
                rwl.readLock().unlock();
            }
        }
    }
}
