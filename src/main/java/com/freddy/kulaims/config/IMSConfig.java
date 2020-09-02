package com.freddy.kulaims.config;

/**
 * IMS配置
 */
public class IMSConfig {

    public static final int CONNECT_TIMEOUT = 10 * 1000;// 连接超时时间，单位：毫秒
    public static final int RECONNECT_INTERVAL = 6 * 1000;// 重连间隔时间，单位：毫秒
    public static final int RECONNECT_COUNT = 5;// 单个地址一个周期最大重连次数
    public static final int FOREGROUND_HEARTBEAT_INTERVAL = 8 * 1000;// 应用在前台时心跳间隔时间，单位：毫秒
    public static final int BACKGROUND_HEARTBEAT_INTERVAL = 30 * 1000;// 应用在后台时心跳间隔时间，单位：毫秒
    public static final boolean AUTO_RESEND = true;// 是否自动重发消息
    public static final int RESEND_INTERVAL = 3 * 1000;// 自动重发间隔时间，单位：毫秒
    public static final int RESEND_COUNT = 5;// 消息最大重发次数
}
