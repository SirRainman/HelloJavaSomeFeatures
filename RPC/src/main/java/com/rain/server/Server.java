package com.rain.server;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Program: rain-java-ideas
 * @Description: 服务端
 * @Author: HouHao Ye
 * @Create: 2021-03-01 19:37
 **/
@Slf4j
public class Server {
    private static boolean isRunning = true;

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8888);

        // 服务匹配表
        HandlerMap.addHandler("com.rain.common.UserService", UserServiceImpl.class);

        while(isRunning) {
            Socket socket = serverSocket.accept();
            processRequest(socket);
            socket.close();
        }
        serverSocket.close();
    }

    public static void processRequest(Socket socket) throws Exception {
        InputStream is = socket.getInputStream();


        // TODO: 反序列化
        ObjectInputStream ois = new ObjectInputStream(is);
        // TODO: 从反序列化结果中拿到关注的信息
        String clazzName = ois.readUTF();
        String methodName = ois.readUTF();
        Class[] parametersTypes = (Class[]) ois.readObject();
        Object[] args = (Object[]) ois.readObject();



        // TODO: 根据传递过来的类名，寻找相应的处理类去处理RPC请求
        //Class handlerClazz = HandlerMap.findHandlerClass(clazzName);
        Class handlerClazz = UserServiceImpl.class;
        Method method = handlerClazz.getMethod(methodName, parametersTypes);
        // TODO：对请求进行处理
        Object result = method.invoke(handlerClazz.newInstance(), args);


        log.info("{}", result);

        // TODO: 序列化结果，并返回
        OutputStream os = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(result);
        oos.flush();

        ois.close();
        oos.close();
    }
}
