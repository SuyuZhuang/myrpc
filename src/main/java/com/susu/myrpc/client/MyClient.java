package com.susu.myrpc.client;

import com.susu.myrpc.api.ISayHello;
import com.susu.myrpc.lib.RPCClient;

import java.io.IOException;

/**
 * @author SuyuZhuang
 * @date 2020/5/3 7:45 下午
 */
public class MyClient {
    public static void main(String[] args) throws IOException {
        ISayHello service = new RPCClient<>(ISayHello.class).getRef();
        service.sayHello();
    }
}
