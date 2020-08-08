package com.freddy.kulaims.listener;

/**
 * @author FreddyChen
 * @name IMS连接状态监听器
 * @date 2020/05/21 16:32
 * @email chenshichao@outlook.com
 * @github https://github.com/FreddyChen
 */
public interface IMSConnectStatusListener {
    void onUnconnected();
    void onConnecting();
    void onConnected();
    void onConnectFailed();
}
