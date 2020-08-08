package com.freddy.kulaims.netty.tcp;

import android.content.Context;

import com.freddy.kulaims.bean.IMSMsg;
import com.freddy.kulaims.config.IMSOptions;
import com.freddy.kulaims.interf.IMSInterface;
import com.freddy.kulaims.listener.IMSConnectStatusListener;
import com.freddy.kulaims.listener.IMSMsgReceivedListener;
import com.freddy.kulaims.listener.IMSMsgSentStatusListener;

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
    public IMSInterface init(Context context, IMSOptions options, IMSConnectStatusListener connectStatusListener, IMSMsgReceivedListener msgReceivedListener) {
        return this;
    }

    @Override
    public void connect() {
        this.reconnect(true);
    }

    @Override
    public void reconnect(boolean isFirstConnect) {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void sendMsg(IMSMsg msg) {

    }

    @Override
    public void sendMsg(IMSMsg msg, IMSMsgSentStatusListener listener) {

    }

    @Override
    public void sendMsg(IMSMsg msg, boolean isJoinResendManager) {

    }

    @Override
    public void sendMsg(IMSMsg msg, IMSMsgSentStatusListener listener, boolean isJoinResendManager) {

    }

    @Override
    public void release() {

    }
}
