package com.freddy.kulaims.mina.tcp;

import android.annotation.SuppressLint;
import android.content.Context;

import com.freddy.kulaims.bean.IMSMsg;
import com.freddy.kulaims.config.IMSOptions;
import com.freddy.kulaims.interf.IMSInterface;
import com.freddy.kulaims.listener.IMSConnectStatusListener;
import com.freddy.kulaims.listener.IMSMsgReceivedListener;
import com.freddy.kulaims.listener.IMSMsgSentStatusListener;

import io.netty.channel.Channel;

public class MinaTCPIMS implements IMSInterface {

    private MinaTCPIMS() {
    }

    public static MinaTCPIMS getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        private static final MinaTCPIMS INSTANCE = new MinaTCPIMS();
    }

    @Override
    public boolean init(Context context, IMSOptions options, IMSConnectStatusListener connectStatusListener, IMSMsgReceivedListener msgReceivedListener) {
        return false;
    }

    @Override
    public void connect() {

    }

    @Override
    public void reconnect(boolean isFirstConnect) {

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
