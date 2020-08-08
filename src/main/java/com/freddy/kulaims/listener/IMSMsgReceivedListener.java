package com.freddy.kulaims.listener;

import com.freddy.kulaims.bean.IMSMsg;

/**
 * @author FreddyChen
 * @name IMS消息接收监听器
 * @date 2020/05/21 16:32
 * @email chenshichao@outlook.com
 * @github https://github.com/FreddyChen
 */
public interface IMSMsgReceivedListener {
    void onMsgReceived(IMSMsg msg);
}
