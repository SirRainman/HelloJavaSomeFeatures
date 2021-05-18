package com.rain.server;

import com.rain.server.serviceImpl.BlogServiceImpl;
import com.rain.server.serviceImpl.UserServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @Program: rain-java-ideas
 * @Description:
 * 存放服务接口名与服务端对应的实现类
 * 服务启动时要暴露其相关的实现类0
 * 根据request中的interface调用服务端中相关实现类
 *
 * @Author: HouHao Ye
 * @Create: 2021-03-01 19:51
 **/
public class ServiceMap {
    private static Map<String, Class> handlerMap = new HashMap<>();

    public ServiceMap() {
        addHandler("com.rain.service.UserService", UserServiceImpl.class);
        addHandler("com.rain.service.BlogService", BlogServiceImpl.class);
    }

    public static void addHandler(String clazzName, Class clazz) {
        handlerMap.put(clazzName, clazz);
    }

    public static Class findHandlerClass(String clazzName) {
        // TODO: 维护注册表信息
        return handlerMap.get(clazzName);
    }
}
