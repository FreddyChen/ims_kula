package com.freddy.kulaims.interf;

import android.content.Context;

import com.freddy.kulaims.bean.IMSMsg;
import com.freddy.kulaims.config.IMSOptions;
import com.freddy.kulaims.listener.IMSConnectStatusListener;
import com.freddy.kulaims.listener.IMSMsgReceivedListener;
import com.freddy.kulaims.listener.IMSMsgSentStatusListener;

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

    /**
     * 初始化
     *
     * @param context
     * @param options               IMS初始化配置
     * @param connectStatusListener IMS连接状态监听
     * @param msgReceivedListener   IMS消息接收监听
     */
    IMSInterface init(Context context, IMSOptions options, IMSConnectStatusListener connectStatusListener, IMSMsgReceivedListener msgReceivedListener);

    /**
     * 连接
     */
    void connect();

    /**
     * 重连
     *
     * @param isFirstConnect 是否首次连接
     */
    void reconnect(boolean isFirstConnect);

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 发送消息
     *
     * @param msg
     */
    void sendMsg(IMSMsg msg);

    /**
     * 发送消息
     * 重载
     *
     * @param msg
     * @param listener 消息发送状态监听器
     */
    void sendMsg(IMSMsg msg, IMSMsgSentStatusListener listener);

    /**
     * 发送消息
     * 重载
     *
     * @param msg
     * @param isJoinResendManager 是否加入消息重发管理器
     */
    void sendMsg(IMSMsg msg, boolean isJoinResendManager);

    /**
     * 发送消息
     * 重载
     *
     * @param msg
     * @param listener 消息发送状态监听器
     * @param isJoinResendManager 是否加入消息重发管理器
     */
    void sendMsg(IMSMsg msg, IMSMsgSentStatusListener listener, boolean isJoinResendManager);

    /**
     * 释放资源
     */
    void release();
}
