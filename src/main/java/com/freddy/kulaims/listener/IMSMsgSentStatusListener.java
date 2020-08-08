package com.freddy.kulaims.listener;

import com.freddy.kulaims.bean.IMSMsg;

/**
 * @author FreddyChen
 * @name IMS消息发送状态监听器
 * @date 2020/05/21 16:32
 * @email chenshichao@outlook.com
 * @github https://github.com/FreddyChen
 */
public interface IMSMsgSentStatusListener {

    /**
     * 消息发送成功
     */
    void onSendSucceed(IMSMsg msg);

    /**
     * 消息发送失败
     */
    void onSendFailed(IMSMsg msg, String errMsg);
}
