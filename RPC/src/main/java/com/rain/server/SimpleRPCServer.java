package com.rain.server;

import com.rain.common.RPCRequest;
import com.rain.common.RPCResponse;
import com.rain.server.serviceImpl.UserServiceImpl;
import com.rain.service.RPCServer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Program: rain-java-ideas
 * @Description:
 * 服务端
 * 使用线程池处理rpc请求
 * @Author: HouHao Ye
 * @Create: 2021-03-01 19:37
 **/
@Slf4j
public class SimpleRPCServer implements RPCServer {
    private static boolean isRunning = true;
    // TODO：服务匹配表
    //  所有对服务匹配表对改动都是基于对象的，只能当前范围内可见，并不是全局的，怎么让对服务匹配表对改变对全局可见？？？？
    private ServiceMap serviceMap;

    // TODO: 线程池处理RPC请求
    private final ThreadPoolExecutor threadPool;

    public SimpleRPCServer(ServiceMap serviceMap){
        threadPool = new ThreadPoolExecutor(5,
                5, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(3));
        this.serviceMap = serviceMap;
    }

    // TODO: 线程池的参数优化
    public SimpleRPCServer(ServiceMap serviceMap, int corePoolSize, int maximumPoolSize, long keepAliveTime,
                           TimeUnit timeUnit,
                           BlockingQueue<Runnable> blockingQueue) {
        this.serviceMap = serviceMap;
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, blockingQueue);
    }

    public static void main(String[] args) throws Exception {
        SimpleRPCServer simpleRPCServer = new SimpleRPCServer(new ServiceMap());
        simpleRPCServer.start(8888);
    }

    @Override
    public void start(int port) throws Exception {
        log.info("服务端启动...");
        ServerSocket serverSocket = new ServerSocket(port);

        while(isRunning) {
            Socket socket = serverSocket.accept();
            // TODO: 高并发情况下，使用线程池去处理rpc请求
            // processRequest(socket); // 单线程处理rpc请求
            threadPool.execute(new RPCTask(socket, serviceMap));
        }
    }

    // TODO: 单线程下使用该方法处理rpc请求，多线程下使用线程池去处理rpc请求
    private void processRequest(Socket socket) throws Exception {}

    @Override
    public void stop() {
        log.info("终止服务端...");
        isRunning = false;
    }
}
