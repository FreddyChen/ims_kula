package com.freddy.kulaims.netty.tcp;

import com.freddy.kulaims.interf.IMSInterface;

/**
 * @author FreddyChen
 * @name Netty TCP IM Service
 * @date 2020/05/21 16:33
 * @email chenshichao@outlook.com
 * @github https://github.com/FreddyChen
 * @desc 基于Netty实现的TCP协议客户端
 */
public class NettyTCPIMS implements IMSInterface {

    private NettyTCPIMS() {
    }

    public static NettyTCPIMS getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final NettyTCPIMS INSTANCE = new NettyTCPIMS();
    }

    @Override
    public void init() {
    }
}
