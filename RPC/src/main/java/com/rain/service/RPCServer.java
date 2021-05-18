package com.rain.service;

/**
 * @program: rain-java-ideas
 * @description:
 * @author: Rain
 * @create: 2021-03-15 20:16
 **/
public interface RPCServer {
    void start(int port) throws Exception;
    void stop();
}
