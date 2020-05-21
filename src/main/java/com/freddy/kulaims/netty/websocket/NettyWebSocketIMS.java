package com.freddy.kulaims.netty.websocket;

import com.freddy.kulaims.interf.IMSInterface;

/**
 * @author FreddyChen
 * @name Netty TCP IM Service
 * @date 2020/05/21 16:33
 * @email chenshichao@outlook.com
 * @github https://github.com/FreddyChen
 * @desc 基于Netty实现的WebSocket协议客户端
 */
public class NettyWebSocketIMS implements IMSInterface {

    private NettyWebSocketIMS() { }

    public static NettyWebSocketIMS getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final NettyWebSocketIMS INSTANCE = new NettyWebSocketIMS();
    }

    @Override
    public void init() {

    }
}
