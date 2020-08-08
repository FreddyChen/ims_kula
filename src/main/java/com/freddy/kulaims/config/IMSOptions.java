package com.freddy.kulaims.config;

import java.util.List;

/**
 * IMS初始化配置项
 */
public class IMSOptions {

    private CommunicationProtocol communicationProtocol;// 通信协议
    private TransportProtocol transportProtocol;// 传输协议
    private int connectTimeout;// 连接超时时间，单位：毫秒
    private int reconnectInterval;// 重连间隔时间，单位：毫秒
    private int reconnectCount;// 单个地址一个周期最大重连次数
    private int foregroundHeartbeatInterval;// 应用在前台时心跳间隔时间，单位：毫秒
    private int backgroundHeartbeatInterval;// 应用在后台时心跳间隔时间，单位：毫秒
    private boolean autoResend;// 是否自动重发消息
    private int resendInterval;// 自动重发间隔时间，单位：毫秒
    private int resendCount;// 消息最大重发次数
    private List<String> serverList;// 服务器地址列表

    private IMSOptions(Builder builder) {
        if (builder == null) return;
        this.communicationProtocol = builder.communicationProtocol;
        this.transportProtocol = builder.transportProtocol;
        this.connectTimeout = builder.connectTimeout;
        this.reconnectInterval = builder.reconnectInterval;
        this.reconnectCount = builder.reconnectCount;
        this.foregroundHeartbeatInterval = builder.foregroundHeartbeatInterval;
        this.backgroundHeartbeatInterval = builder.backgroundHeartbeatInterval;
        this.autoResend = builder.autoResend;
        this.resendInterval = builder.resendInterval;
        this.resendCount = builder.resendCount;
        this.serverList = builder.serverList;
    }

    public CommunicationProtocol getCommunicationProtocol() {
        return communicationProtocol;
    }

    public TransportProtocol getTransportProtocol() {
        return transportProtocol;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReconnectInterval() {
        return reconnectInterval;
    }

    public int getReconnectCount() {
        return reconnectCount;
    }

    public int getForegroundHeartbeatInterval() {
        return foregroundHeartbeatInterval;
    }

    public int getBackgroundHeartbeatInterval() {
        return backgroundHeartbeatInterval;
    }

    public boolean isAutoResend() {
        return autoResend;
    }

    public int getResendInterval() {
        return resendInterval;
    }

    public int getResendCount() {
        return resendCount;
    }

    public List<String> getServerList() {
        return serverList;
    }

    public static class Builder {

        private CommunicationProtocol communicationProtocol;// 通信协议
        private TransportProtocol transportProtocol;// 传输协议
        private int connectTimeout;// 连接超时时间，单位：毫秒
        private int reconnectInterval;// 重连间隔时间，单位：毫秒
        private int reconnectCount;// 单个地址一个周期最大重连次数
        private int foregroundHeartbeatInterval;// 应用在前台时心跳间隔时间，单位：毫秒
        private int backgroundHeartbeatInterval;// 应用在后台时心跳间隔时间，单位：毫秒
        private boolean autoResend;// 是否自动重发消息
        private int resendInterval;// 自动重发间隔时间，单位：毫秒
        private int resendCount;// 消息最大重发次数
        private List<String> serverList;// 服务器地址列表

        public Builder() {
            this.connectTimeout = IMSConfig.CONNECT_TIMEOUT;
            this.reconnectInterval = IMSConfig.RECONNECT_INTERVAL;
            this.reconnectCount = IMSConfig.RECONNECT_COUNT;
            this.foregroundHeartbeatInterval = IMSConfig.FOREGROUND_HEARTBEAT_INTERVAL;
            this.backgroundHeartbeatInterval = IMSConfig.BACKGROUND_HEARTBEAT_INTERVAL;
            this.autoResend = IMSConfig.AUTO_RESEND;
            this.resendInterval = IMSConfig.RESEND_INTERVAL;
            this.resendCount = IMSConfig.RESEND_COUNT;
        }

        public Builder setCommunicationProtocol(CommunicationProtocol communicationProtocol) {
            this.communicationProtocol = communicationProtocol;
            return this;
        }

        public Builder setTransportProtocol(TransportProtocol transportProtocol) {
            this.transportProtocol = transportProtocol;
            return this;
        }

        public Builder setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setReconnectInterval(int reconnectInterval) {
            this.reconnectInterval = reconnectInterval;
            return this;
        }

        public Builder setReconnectCount(int reconnectCount) {
            this.reconnectCount = reconnectCount;
            return this;
        }

        public Builder setForegroundHeartbeatInterval(int foregroundHeartbeatInterval) {
            this.foregroundHeartbeatInterval = foregroundHeartbeatInterval;
            return this;
        }

        public Builder setBackgroundHeartbeatInterval(int backgroundHeartbeatInterval) {
            this.backgroundHeartbeatInterval = backgroundHeartbeatInterval;
            return this;
        }

        public Builder setAutoResend(boolean autoResend) {
            this.autoResend = autoResend;
            return this;
        }

        public Builder setResendInterval(int resendInterval) {
            this.resendInterval = resendInterval;
            return this;
        }

        public Builder setResendCount(int resendCount) {
            this.resendCount = resendCount;
            return this;
        }

        public Builder setServerList(List<String> serverList) {
            this.serverList = serverList;
            return this;
        }

        public IMSOptions build() {
            return new IMSOptions(this);
        }
    }
}
