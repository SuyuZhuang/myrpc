package com.susu.myrpc.lib;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author SuyuZhuang
 * @date 2020/5/3 11:41 下午
 */
public class NIOProbiver<T> {
    private T serviceImpl;
    private ServerSocketChannel channel;

    public NIOProbiver(T serviceImpl) throws IOException {
        this.serviceImpl = serviceImpl;
        this.channel = ServerSocketChannel.open();
        this.channel.configureBlocking(false);
        this.channel.bind(new InetSocketAddress(8888));
    }

    private static class SocketData{
        StringBuilder sb = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        boolean isReading = true;

        public void append(int readBytes) {
            byte[] tmp = new byte[readBytes];
            buffer.get(tmp);
            sb.append(new String(tmp));
        }
    }

    public void start() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectionKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel oneClient = channel.accept();
                    oneClient.configureBlocking(false);
                    oneClient.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new SocketData());
                }

                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    SocketData attachment = (SocketData) key.attachment();
                    int readBytes = client.read(attachment.buffer);
                    if (readBytes > 0) {
                        // 切换到读模式
                        attachment.buffer.flip();
                        attachment.append(readBytes);

                        // 读到换行符，说明数据都读完了
                        if (attachment.sb.toString().contains("\n")) {
                            String line = attachment.sb.toString();
                            System.out.println("NIOProvider 读到的数据: " + line);
                            MethodInfo methodInfo = JSON.parseObject(line, MethodInfo.class);
                            // 知道客户端想要我们调用什么方法了
                            Method method = serviceImpl.getClass().getMethod(methodInfo.getMethodName(),
                                    methodInfo.params.stream().map(Object::getClass).toArray(Class[]::new));
                            Object returnValue = method.invoke(serviceImpl, methodInfo.params.toArray());
                            byte[] returnValueBytes = (JSON.toJSONString(returnValue) + "\n").getBytes();
                            System.out.println("准备写：" + new String(returnValueBytes));
                            // 切换写模式
                            attachment.buffer.flip();
                            attachment.isReading = false;
                            attachment.buffer.put(returnValueBytes);
                        }
                    } else if (readBytes < 0) {
                        client.close();
                    }
                }

                if (key.isWritable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    SocketData socketData = (SocketData) key.attachment();
                    if (!socketData.isReading) {
                        socketData.isReading = true;
                        socketData.buffer.flip();

                        while (socketData.buffer.hasRemaining()){
                            client.write(socketData.buffer);
                        }
                        client.close();
                    }
                }
            }
        }
    }

}
