package com.rain.client;

import com.rain.common.RPCRequest;
import com.rain.common.RPCResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @Program: rain-java-ideas
 * @Description:
 * 负责底层与服务端的通信，发送的Request，接受的是Response对象
 * 客户端发起一次请求调用，Socket建立连接，发起请求Request，得到响应Response
 * request是封装好的（上层进行封装），不同的service需要进行不同的封装，
 * 客户端只知道Service接口，需要一层动态代理根据反射封装不同的Service
 * @Author: HouHao Ye
 * @Create: 2021-03-15
 **/
@Slf4j
public class IOClient {

    public static RPCResponse sendRequest(String host, int port, RPCRequest request){
        try {
            Socket socket = new Socket(host, port);

            // TODO：发送请求
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            // TODO：序列化请求
            oos.writeObject(request);
            oos.flush();
            log.info("Client sent an RPC request {} to {}:{}", request, host, port);

            // TODO：接收来自服务器的处理结果
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            RPCResponse response = (RPCResponse) ois.readObject();
            ois.close();

            // TODO: socket链接的关闭顺序是什么？
            oos.close();
            socket.close();
            return response;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
