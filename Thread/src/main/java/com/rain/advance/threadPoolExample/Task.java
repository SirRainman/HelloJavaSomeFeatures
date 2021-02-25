package com.rain.advance.threadPoolExample;

import com.rain.util.Sleeper;
import lombok.extern.slf4j.Slf4j;

/**
 * @program: rain-java-ideas
 * @description: 需要执行的任务
 * @author: Rain
 * @create: 2021-02-24 17:28
 **/
@Slf4j(topic = "c.Task")
public class Task implements Runnable {
    int taskID;

    public Task(int taskID) {
        this.taskID = taskID;
    }

    @Override
    public void run() {
        log.info("task{} is being executed", taskID);
        Sleeper.sleep(2);
    }

    @Override
    public String toString() {
        return "task" + taskID;
    }
}
