package com.freddy.kulaims.interf;

/**
 * @author FreddyChen
 * @name IMS抽象接口
 * @date 2020/05/21 16:32
 * @email chenshichao@outlook.com
 * @github https://github.com/FreddyChen
 * @desc 不同的客户端协议实现此接口即可，例：
 * {@link com.freddy.kulaims.netty.tcp.NettyTCPIMS}
 * {@link com.freddy.kulaims.netty.websocket.NettyWebSocketIMS}
 */
public interface IMSInterface {

    void init();
}
