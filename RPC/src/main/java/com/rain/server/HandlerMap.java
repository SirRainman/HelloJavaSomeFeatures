package com.rain.server;

import java.util.HashMap;
import java.util.Map;

/**
 * @Program: rain-java-ideas
 * @Description:
 * @Author: HouHao Ye
 * @Create: 2021-03-01 19:51
 **/
public class HandlerMap {
    private static Map<String, Class> handlerMap = new HashMap<>();

    public HandlerMap() {}

    public static void addHandler(String clazzName, Class clazz) {
        handlerMap.put(clazzName, clazz);
    }

    public static Class findHandlerClass(String clazzName) {
        // TODO: 维护注册表信息
        return handlerMap.get(clazzName);
    }
}
