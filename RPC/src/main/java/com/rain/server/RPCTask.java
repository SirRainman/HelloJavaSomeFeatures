package com.rain.server;

import com.rain.common.RPCRequest;
import com.rain.common.RPCResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @program: rain-java-ideas
 * @description:
 * 一个rpc任务，规范如何处理rpc请求
 * @author: Rain
 * @create: 2021-03-15 20:36
 **/
@Slf4j
@AllArgsConstructor
public class RPCTask implements Runnable {
    private Socket socket;
    private ServiceMap serviceMap;

    @Override
    public void run() {
        try {
            // TODO: 反序列化
            //  从反序列化结果中拿到关注的信息
            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            RPCRequest rpcRequest = (RPCRequest) ois.readObject();

            // TODO: 序列化结果，并返回
            RPCResponse rpcResponse = handleRPCRequest(rpcRequest);
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(rpcResponse);
            oos.flush();

            ois.close();
            oos.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RPCResponse handleRPCRequest(RPCRequest rpcRequest) {
        log.info("receive request {}", rpcRequest);
        String clazzName = rpcRequest.getInterfaceName();
        String methodName = rpcRequest.getMethodName();
        Class[] parametersTypes = rpcRequest.getParamsTypes();
        Object[] args = rpcRequest.getParams();

        try {
            // TODO: 根据传递过来的类名，寻找相应的处理类，去处理RPC请求
            Class handlerClazz = serviceMap.findHandlerClass(clazzName);
            Method method = handlerClazz.getMethod(methodName, parametersTypes);

            // TODO：对请求进行处理
            Object result = method.invoke(handlerClazz.newInstance(), args);
            log.info("{}", result);
            return RPCResponse.success(result);
        } catch (Exception e) {
            log.error("方法执行错误");
            e.printStackTrace();
            return RPCResponse.fail();
        }
    }
}
