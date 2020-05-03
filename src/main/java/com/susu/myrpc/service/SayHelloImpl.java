package com.susu.myrpc.service;

import com.susu.myrpc.api.ISayHello;

/**
 * @author SuyuZhuang
 * @date 2020/5/3 7:44 下午
 */
public class SayHelloImpl implements ISayHello {
    @Override
    public String sayHello() {
        return "Hello My RPC";
    }
}
