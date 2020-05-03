package com.susu.myrpc.provider;

import com.susu.myrpc.lib.RPCProvider;
import com.susu.myrpc.service.SayHelloImpl;

import java.io.IOException;

/**
 * @author SuyuZhuang
 * @date 2020/5/3 7:45 下午
 */
public class MyProvider {

    public static void main(String[] args) throws IOException {
        SayHelloImpl impl = new SayHelloImpl();
        new RPCProvider<>(impl).start();
        System.in.read();
    }
}
