package com.susu.myrpc.lib;

import com.alibaba.fastjson.JSON;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author SuyuZhuang
 * @date 2020/5/3 7:46 下午
 */
public class RPCClient<T> {
    private Class<T> providerInterfaceClass;
    private Socket socket;

    public RPCClient(Class<T> providerInterfaceClass) throws IOException {
        this.providerInterfaceClass = providerInterfaceClass;
        this.socket = new Socket();
        this.socket.connect(new InetSocketAddress("127.0.0.1", 8888));
    }

    public T getRef(){
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{providerInterfaceClass},
                (proxy, method, args) -> {
                    // 发送客户端想使用的接口数据
                    MethodInfo methodInfo = new MethodInfo(method.getName(), Stream.of(args).collect(Collectors.toList()));
                    RPCClient.this.socket.getOutputStream().write((JSON.toJSONString(methodInfo) + "\n").getBytes());
                    RPCClient.this.socket.getOutputStream().flush();

                    // 拿到结果
                    String retrunedValue = new BufferedReader(new InputStreamReader(RPCClient.this.socket.getInputStream())).readLine();
                    return JSON.parse(retrunedValue);
                });
    }
}
