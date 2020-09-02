package com.freddy.kulaims.netty.tcp;

import android.util.Log;

import com.freddy.kulaims.R;
import com.freddy.kulaims.config.IMSConnectStatus;
import com.freddy.kulaims.config.IMSOptions;

import java.util.List;
import java.util.Random;

import io.netty.channel.Channel;
import io.netty.util.internal.StringUtil;

public class NettyTCPReconnectTask implements Runnable {

    private static final String TAG = NettyTCPReconnectTask.class.getSimpleName();
    private NettyTCPIMS ims;
    private IMSOptions imsOptions;

    NettyTCPReconnectTask(NettyTCPIMS ims) {
        this.ims = ims;
        this.imsOptions = ims.getIMSOptions();
    }

    @Override
    public void run() {
        try {
            // 重连时，释放工作线程组，也就是停止心跳
            ims.getExecutors().destroyWorkLoopGroup();

            // ims未关闭并且网络可用的情况下，才去连接
            while (!ims.isClosed() && ims.isNetworkAvailable()) {
                IMSConnectStatus status;
                if ((status = connect()) == IMSConnectStatus.Connected) {
                    ims.callbackIMSConnectStatus(status);
                    break;// 连接成功，跳出循环
                }

                if (status == IMSConnectStatus.ConnectFailed
                        || status == IMSConnectStatus.ConnectFailed_IMSClosed
                        || status == IMSConnectStatus.ConnectFailed_ServerListEmpty
                        || status == IMSConnectStatus.ConnectFailed_NetworkUnavailable) {
                    ims.callbackIMSConnectStatus(status);
                    // 一个周期连接失败后，延时指定之间再去进行下一个周期的连接
                    try {
                        Thread.sleep(imsOptions.getReconnectInterval());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            // 标识重连任务停止
            ims.setReconnecting(false);
        }
    }

    private IMSConnectStatus connect() {
        if (ims.isClosed()) return IMSConnectStatus.ConnectFailed_IMSClosed;
        ims.initBootstrap();
        List<String> serverList = imsOptions.getServerList();
        if (serverList == null || serverList.isEmpty()) {
            return IMSConnectStatus.ConnectFailed_ServerListEmpty;
        }

        for (String server : serverList) {
            if (StringUtil.isNullOrEmpty(server)) {
                return IMSConnectStatus.ConnectFailed_ServerEmpty;
            }

            String[] params = null;
            try {
                params = server.split(" ");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (params == null || params.length < 2) {
                return IMSConnectStatus.ConnectFailed_ServerIllegitimate;
            }

            // +1是因为首次连接也认为是重连，所以如果重连次数设置为3，则最大连接次数为3+1次
            for (int i = 0; i < imsOptions.getReconnectCount() + 1; i++) {
                if (ims.isClosed()) {
                    return IMSConnectStatus.ConnectFailed_IMSClosed;
                }
                if (!ims.isNetworkAvailable()) {
                    return IMSConnectStatus.ConnectFailed_NetworkUnavailable;
                }

                if (i == 0) {
                    ims.callbackIMSConnectStatus(IMSConnectStatus.Connecting);
                }
                Log.d(TAG, String.format("正在进行【%1$s】的第%2$d次连接", server, i + 1));
                try {
                    String host = params[0];
                    int port = Integer.parseInt(params[1]);
                    Channel channel = toServer(host, port);
                    if (channel != null && channel.isOpen() && channel.isActive() && channel.isRegistered() && channel.isWritable()) {
                        ims.setChannel(channel);
                        return IMSConnectStatus.Connected;
                    } else {
                        if (i == imsOptions.getReconnectCount() - 1) {
                            return IMSConnectStatus.ConnectFailed;
                        }

                        // 连接失败，则线程休眠n * 重连间隔时长
                        int delayTime = imsOptions.getReconnectInterval() + imsOptions.getReconnectInterval() / 2 * i;
                        Log.w(TAG, "正在等待重连，当前重连延时时长：" + delayTime + "ms");
                        Thread.sleep(delayTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    ims.release();
                    break;// 线程被中断，则强制关闭
                }
            }
        }

        return IMSConnectStatus.ConnectFailed;
    }

    private Channel toServer(String host, int port) {
        Channel channel;
        try {
            channel = ims.getBootstrap().connect(host, port).sync().channel();
        } catch (Exception e) {
            e.printStackTrace();
            channel = null;
        }

        return channel;
    }
}
