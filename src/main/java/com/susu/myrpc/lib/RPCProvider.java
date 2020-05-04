package com.susu.myrpc.lib;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author SuyuZhuang
 * @date 2020/5/3 7:46 下午
 */
public class RPCProvider<T> {
    private T serviceImpl;
    private ServerSocket ss;

    public RPCProvider(T serviceImpl) throws IOException {
        this.serviceImpl = serviceImpl;
        this.ss = new ServerSocket();
        ss.bind(new InetSocketAddress("127.0.0.1", 8888));
    }

    public void start() throws IOException {
        while (true) {
            Socket socket = ss.accept();
            new WorkerThread(socket).start();
        }
    }

    class WorkerThread extends Thread{
        private Socket socket;
        public WorkerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line = bufferedReader.readLine();
                System.out.println("RPCProvider 线程开启: " + line);
                MethodInfo methodInfo = JSON.parseObject(line, MethodInfo.class);
                // 知道客户端想要我们调用什么方法了
                Method method = serviceImpl.getClass().getMethod(methodInfo.getMethodName(),
                        methodInfo.params.stream().map(Object::getClass).toArray(Class[]::new));
                Object returnValue = method.invoke(serviceImpl, methodInfo.params.toArray());
                socket.getOutputStream().write((JSON.toJSONString(returnValue) + "\n").getBytes());
                socket.getOutputStream().flush();
            } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
