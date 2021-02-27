package com.rain.advance;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.StampedLock;

import static com.rain.util.Sleeper.sleep;

/**
 * @Program: rain-java-ideas
 * @Description:
 * @Author: HouHao Ye
 * @Create: 2021-02-27 19:45
 **/
@Slf4j
public class SampledLockExample {
    class DataContainerStamped {
        private int data;
        private final StampedLock lock = new StampedLock();

        public DataContainerStamped(int data) {
            this.data = data;
        }

        public int read(int readTime) {
            long stamp = lock.tryOptimisticRead();
            log.debug("optimistic read locking...{}", stamp);
            sleep(readTime);
            if (lock.validate(stamp)) {
                log.debug("read finish...{}, data:{}", stamp, data);
                return data;
            }
            // 锁升级 - 读锁
            log.debug("updating to read lock... {}", stamp);
            try {
                stamp = lock.readLock();
                log.debug("read lock {}", stamp);
                sleep(readTime);
                log.debug("read finish...{}, data:{}", stamp, data);
                return data;
            } finally {
                log.debug("read unlock {}", stamp);
                lock.unlockRead(stamp);
            }
        }

        public void write(int newData) {
            long stamp = lock.writeLock();
            log.debug("write lock {}", stamp);
            try {
                sleep(2);
                this.data = newData;
            } finally {
                log.debug("write unlock {}", stamp);
                lock.unlockWrite(stamp);
            }
        }
    }
}
