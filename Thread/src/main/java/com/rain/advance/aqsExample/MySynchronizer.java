package com.rain.advance.aqsExample;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-02-25 13:38
 **/
public class MySynchronizer extends AbstractQueuedSynchronizer {
    @Override
    protected boolean tryAcquire(int acquires) {
        if (acquires == 1) {
            if (compareAndSetState(0, 1)) { // 通过CAS方式进行修改，这里修改的是state吗？
                setExclusiveOwnerThread(Thread.currentThread()); // 设置为Owner
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean tryRelease(int acquires) {
        if (acquires == 1) {
            if(getState() == 0) { // state 是 volatile 类型的变量;
                throw new IllegalMonitorStateException();
            }
            setExclusiveOwnerThread(null);
            // TODO：这里有一个细节，state是volatile类型的变量，
            //  这里对volatile变量进行修改后，会有一个写屏障，前面所有对共享变量对修改都会写入到内存区
            setState(0);
            return true;
        }
        return false;
    }

    // 判断是否是独占锁
    @Override
    protected boolean isHeldExclusively() {
        return getState() == 1;
    }

    protected Condition newCondition() {
        return new ConditionObject();
    }
}
