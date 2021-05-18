package com.rain.client;

import com.rain.common.RPCRequest;
import com.rain.common.RPCResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Program: rain-java-ideas
 * @Description:
 * @Author: HouHao Ye
 * @Create: 2021-03-01 20:10
 **/
@Slf4j
public class Stub {
    public static Object getStub(Class clazz) {

        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // TODO: 去服务注册中心找服务相对应的ip和端口
                String host = "127.0.0.1";
                int port = 8888;

                // TODO：获取 远程过程调用需要用的方法的 类、参数类型、参数
                String clazzName = clazz.getName();
                String methodName = method.getName();
                Class[] parametersTypes = method.getParameterTypes();

                // TODO: 封装请求
                //  这里要注意发送消息的格式，这个格式是发送方/接收方共同遵守的
                RPCRequest rpcRequest = RPCRequest.builder().build();
                rpcRequest.setInterfaceName(clazzName);
                rpcRequest.setMethodName(methodName);
                rpcRequest.setParamsTypes(parametersTypes);
                rpcRequest.setParams(args);

                // TODO: 使用相应的通信协议发送请求
                //  并等待接收远程过程调用的结果
                RPCResponse rpcResponse = IOClient.sendRequest(host, port, rpcRequest);
                return rpcResponse.getData();
            }
        };

        Object object = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
        return object;
    }
}
