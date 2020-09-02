package com.freddy.kulaims.netty.tcp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.freddy.kulaims.bean.IMSMsg;
import com.freddy.kulaims.config.IMSConnectStatus;
import com.freddy.kulaims.config.IMSOptions;
import com.freddy.kulaims.interf.IMSInterface;
import com.freddy.kulaims.listener.IMSConnectStatusListener;
import com.freddy.kulaims.listener.IMSMsgReceivedListener;
import com.freddy.kulaims.listener.IMSMsgSentStatusListener;
import com.freddy.kulaims.net.NetworkManager;
import com.freddy.kulaims.utils.ExecutorServiceFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author FreddyChen
 * @name Netty TCP IM Service
 * @date 2020/05/21 16:33
 * @email chenshichao@outlook.com
 * @github https://github.com/FreddyChen
 * @desc 基于Netty实现的TCP协议客户端
 */
public class NettyTCPIMS implements IMSInterface, NetworkManager.INetworkStateChangedObserver {

    private static final String TAG = NettyTCPIMS.class.getSimpleName();
    private Context mContext;
    private IMSOptions mIMSOptions;
    private IMSConnectStatusListener mIMSConnectStatusListener;
    private IMSMsgReceivedListener mIMSMsgReceivedListener;
    private volatile boolean isClosed = true;
    private volatile boolean isReconnecting = false;
    private boolean initialized = false;// 是否已初始化成功
    private Bootstrap bootstrap;
    private Channel channel;
    private volatile IMSConnectStatus imsConnectStatus;
    private ExecutorServiceFactory executors;
    private boolean isNetworkAvailable;

    private NettyTCPIMS() {
    }

    public static NettyTCPIMS getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        private static final NettyTCPIMS INSTANCE = new NettyTCPIMS();
    }

    @Override
    public void onAvailable() {
        Log.d(TAG, "网络可用");
        this.isNetworkAvailable = true;
    }

    @Override
    public void onUnavailable() {
        Log.d(TAG, "网络不可用");
        this.isNetworkAvailable = false;
    }

    @Override
    public boolean init(Context context, IMSOptions options, IMSConnectStatusListener connectStatusListener, IMSMsgReceivedListener msgReceivedListener) {
        if (context == null) {
            Log.d(TAG, "初始化失败：Context is null.");
            initialized = false;
            return false;
        }

        if (options == null) {
            Log.d(TAG, "初始化失败：IMSOptions is null.");
            initialized = false;
            return false;
        }
        this.mContext = context;
        this.mIMSOptions = options;
        this.mIMSConnectStatusListener = connectStatusListener;
        this.mIMSMsgReceivedListener = msgReceivedListener;
        executors = new ExecutorServiceFactory();
        executors.initBossLoopGroup();// 初始化重连线程组
        NetworkManager.getInstance().registerObserver(context, this);
        initialized = true;
        isClosed = false;
        return true;
    }

    @Override
    public void connect() {
        if(!initialized) {
            Log.w(TAG, "IMS初始化失败，请查看日志");
            return;
        }
        this.reconnect(true);
    }

    @Override
    public void reconnect(boolean isFirstConnect) {
        callbackIMSConnectStatus(IMSConnectStatus.Unconnected);
        // 非首次重连，代表之前已经进行过重连，延时一段时间再去重连
        if (!isFirstConnect) {
            // 非首次进行重连，执行到这里即代表已经连接失败，回调连接状态到应用层
            callbackIMSConnectStatus(IMSConnectStatus.ConnectFailed);

            try {
                Thread.sleep(mIMSOptions.getReconnectInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!isClosed && !isReconnecting) {
            synchronized (this) {
                if (!isClosed && !isReconnecting) {
                    setReconnecting(true);
                    closeChannel();
                    executors.execBossTask(new NettyTCPReconnectTask(this));
                }
            }
        }
    }

    @Override
    public void sendMsg(IMSMsg msg) {
        this.sendMsg(msg, null, true);
    }

    @Override
    public void sendMsg(IMSMsg msg, IMSMsgSentStatusListener listener) {
        this.sendMsg(msg, listener, true);
    }

    @Override
    public void sendMsg(IMSMsg msg, boolean isJoinResendManager) {
        this.sendMsg(msg, null, isJoinResendManager);
    }

    @Override
    public void sendMsg(IMSMsg msg, IMSMsgSentStatusListener listener, boolean isJoinResendManager) {
        if(!initialized) {
            Log.w(TAG, "IMS初始化失败，请查看日志");
            return;
        }
    }

    @Override
    public void release() {
        closeChannel();
        closeBootstrap();
        if(executors != null) {
            executors.destroy();
            executors = null;
        }
        NetworkManager.getInstance().unregisterObserver(mContext, this);
    }

    /**
     * 初始化bootstrap
     */
    void initBootstrap() {
        closeBootstrap();// 初始化前先关闭
        NioEventLoopGroup loopGroup = new NioEventLoopGroup(4);
        bootstrap = new Bootstrap();
        bootstrap.group(loopGroup).channel(NioSocketChannel.class)
                // 设置该选项以后，如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
                .option(ChannelOption.SO_KEEPALIVE, true)
                // 设置禁用nagle算法，如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false
                .option(ChannelOption.TCP_NODELAY, true)
                // 设置TCP发送缓冲区大小（字节数）
                .option(ChannelOption.SO_SNDBUF, 32 * 1024)
                // 设置TCP接收缓冲区大小（字节数）
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                // 设置连接超时时长，单位：毫秒
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, mIMSOptions.getConnectTimeout())
                // 设置初始化ChannelHandler
                .handler(new NettyTCPChannelInitializerHandler());
    }

    /**
     * 关闭channel
     */
    private void closeChannel() {
        try {
            if (channel != null) {
                try {
                    channel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    channel.eventLoop().shutdownGracefully();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }finally {
            channel = null;
        }
    }

    /**
     * 关闭bootstrap
     */
    void closeBootstrap() {
        try {
            if (bootstrap != null) {
                bootstrap.config().group().shutdownGracefully();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bootstrap = null;
        }
    }

    /**
     * 回调ims连接状态
     *
     * @param imsConnectStatus
     */
    void callbackIMSConnectStatus(IMSConnectStatus imsConnectStatus) {
        this.imsConnectStatus = imsConnectStatus;
        switch (imsConnectStatus) {
            case Unconnected:
                Log.w(TAG, "IMS未连接");
                if (mIMSConnectStatusListener != null) {
                    mIMSConnectStatusListener.onUnconnected();
                }
                break;

            case Connecting:
                Log.d(TAG, "IMS连接中");
                if (mIMSConnectStatusListener != null) {
                    mIMSConnectStatusListener.onConnecting();
                }
                break;

            case Connected:
                Log.d(TAG, "IMS连接成功");
                if (mIMSConnectStatusListener != null) {
                    mIMSConnectStatusListener.onConnected();
                }
                break;

            case ConnectFailed:
                Log.w(TAG, "IMS连接失败");
                if (mIMSConnectStatusListener != null) {
                    mIMSConnectStatusListener.onConnectFailed();
                }
                break;
        }
    }

    ExecutorServiceFactory getExecutors() {
        return executors;
    }

    /**
     * 网络是否可用
     *
     * @return
     */
    boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }

    /**
     * ims是否关闭
     *
     * @return
     */
    boolean isClosed() {
        return isClosed;
    }

    IMSOptions getIMSOptions() {
        return mIMSOptions;
    }

    Bootstrap getBootstrap() {
        return bootstrap;
    }

    void setChannel(Channel channel) {
        this.channel = channel;
    }

    void setReconnecting(boolean isReconnecting) {
        this.isReconnecting = isReconnecting;
    }
}
