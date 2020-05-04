package com.susu.myrpc.provider;

import com.susu.myrpc.lib.NIOProbiver;
import com.susu.myrpc.lib.RPCProvider;
import com.susu.myrpc.service.SayHelloImpl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author SuyuZhuang
 * @date 2020/5/3 7:45 下午
 */
public class MyProvider {

    public static void main(String[] args) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        SayHelloImpl impl = new SayHelloImpl();
        new NIOProbiver<>(impl).start();
        System.in.read();
    }
}
