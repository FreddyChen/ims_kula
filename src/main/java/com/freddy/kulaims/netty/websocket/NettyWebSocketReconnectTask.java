package com.freddy.kulaims.netty.websocket;

import android.util.Log;

import com.freddy.kulaims.config.IMSConnectStatus;
import com.freddy.kulaims.config.IMSOptions;

import java.net.URI;
import java.util.List;

import io.netty.channel.Channel;
import io.netty.util.internal.StringUtil;

public class NettyWebSocketReconnectTask implements Runnable {

    private static final String TAG = NettyWebSocketReconnectTask.class.getSimpleName();
    private NettyWebSocketIMS ims;
    private IMSOptions mIMSOptions;

    NettyWebSocketReconnectTask(NettyWebSocketIMS ims) {
        this.ims = ims;
        this.mIMSOptions = ims.getIMSOptions();
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
                        || status == IMSConnectStatus.ConnectFailed_ServerEmpty
                        || status == IMSConnectStatus.ConnectFailed_ServerIllegitimate
                        || status == IMSConnectStatus.ConnectFailed_NetworkUnavailable) {
                    ims.callbackIMSConnectStatus(status);

                    if(ims.isClosed() || !ims.isNetworkAvailable()) {
                        return;
                    }
                    // 一个服务器地址列表都连接失败后，说明网络情况可能很差，延时指定时间（重连间隔时间*2）再去进行下一个服务器地址的连接
                    Log.w(TAG, String.format("一个周期连接失败，等待%1$dms后再次尝试重连", mIMSOptions.getReconnectInterval() * 2));
                    try {
                        Thread.sleep(mIMSOptions.getReconnectInterval() * 2);
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

    /**
     * 连接服务器
     * @return
     */
    private IMSConnectStatus connect() {
        if (ims.isClosed()) return IMSConnectStatus.ConnectFailed_IMSClosed;
        if(!ims.isNetworkAvailable()) return IMSConnectStatus.ConnectFailed_NetworkUnavailable;
        List<String> serverList = mIMSOptions.getServerList();
        if (serverList == null || serverList.isEmpty()) {
            return IMSConnectStatus.ConnectFailed_ServerListEmpty;
        }

        ims.initBootstrap();
        for (int i = 0; i < serverList.size(); i++) {
            String server = serverList.get(i);
            if (StringUtil.isNullOrEmpty(server)) {
                return IMSConnectStatus.ConnectFailed_ServerEmpty;
            }

            URI uri;
            try {
                uri = URI.create(server);
            }catch (IllegalArgumentException e) {
                e.printStackTrace();
                if(i == serverList.size() - 1) {
                    Log.w(TAG, String.format("【%1$s】连接失败，地址不合法", server));
                    return IMSConnectStatus.ConnectFailed_ServerIllegitimate;
                }else {
                    Log.w(TAG, String.format("【%1$s】连接失败，地址不合法，正在等待重连，当前重连延时时长：%2$d", server, mIMSOptions.getReconnectInterval()));
                    Log.w(TAG, "=========================================================================================");
                    try {
                        Thread.sleep(mIMSOptions.getReconnectInterval());
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    continue;
                }
            }

            if(!"ws".equals(uri.getScheme())) {
                if(i == serverList.size() - 1) {
                    Log.w(TAG, String.format("【%1$s】连接失败，地址不合法", server));
                    return IMSConnectStatus.ConnectFailed_ServerIllegitimate;
                }else {
                    Log.w(TAG, String.format("【%1$s】连接失败，地址不合法，正在等待重连，当前重连延时时长：%2$d", server, mIMSOptions.getReconnectInterval()));
                    Log.w(TAG, "=========================================================================================");
                    try {
                        Thread.sleep(mIMSOptions.getReconnectInterval());
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    continue;
                }
            }

            if(i == 0) {
                ims.callbackIMSConnectStatus(IMSConnectStatus.Connecting);
            }

            // +1是因为首次连接也认为是重连，所以如果重连次数设置为3，则最大连接次数为3+1次
            for (int j = 0; j < mIMSOptions.getReconnectCount() + 1; j++) {
                if (ims.isClosed()) {
                    return IMSConnectStatus.ConnectFailed_IMSClosed;
                }
                if (!ims.isNetworkAvailable()) {
                    return IMSConnectStatus.ConnectFailed_NetworkUnavailable;
                }

                Log.d(TAG, String.format("正在进行【%1$s】的第%2$d次连接", server, j + 1));
                try {
                    String host = uri.getHost();
                    int port = uri.getPort();
                    Channel channel = toServer(host, port);
                    if (channel != null && channel.isOpen() && channel.isActive() && channel.isRegistered() && channel.isWritable()) {
                        ims.setChannel(channel);
                        return IMSConnectStatus.Connected;
                    } else {
                        if (j == mIMSOptions.getReconnectCount()) {
                            // 如果当前已达到最大重连次数，并且是最后一个服务器地址，则回调连接失败
                            if(i == serverList.size() - 1) {
                                Log.w(TAG, String.format("【%1$s】连接失败", server));
                                return IMSConnectStatus.ConnectFailed;
                            }
                            // 否则，无需回调连接失败，等待一段时间再去进行下一个服务器地址连接即可
                            // 也就是说，当服务器地址列表里的地址都连接失败，才认为是连接失败
                            else {
                                // 一个服务器地址连接失败后，延时指定时间再去进行下一个服务器地址的连接
                                Log.w(TAG, String.format("【%1$s】连接失败，正在等待进行下一个服务器地址的重连，当前重连延时时长：%2$dms", server, mIMSOptions.getReconnectInterval()));
                                Log.w(TAG, "=========================================================================================");
                                Thread.sleep(mIMSOptions.getReconnectInterval());
                            }
                        } else {
                            // 连接失败，则线程休眠（重连间隔时长 / 2 * n） ms
                            int delayTime = mIMSOptions.getReconnectInterval() + mIMSOptions.getReconnectInterval() / 2 * j;
                            Log.w(TAG, String.format("【%1$s】连接失败，正在等待重连，当前重连延时时长：%2$dms", server, delayTime));
                            Thread.sleep(delayTime);
                        }
                    }
                } catch (InterruptedException e) {
                    break;// 线程被中断，则强制关闭
                }
            }
        }

        return IMSConnectStatus.ConnectFailed;
    }

    /**
     * 真正连接服务器的地方
     * @param host
     * @param port
     * @return
     */
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
