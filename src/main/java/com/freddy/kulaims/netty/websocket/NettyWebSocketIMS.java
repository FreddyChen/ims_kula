package com.freddy.kulaims.netty.websocket;

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
import com.freddy.kulaims.netty.tcp.NettyTCPChannelInitializerHandler;
import com.freddy.kulaims.netty.tcp.NettyTCPIMS;
import com.freddy.kulaims.netty.tcp.NettyTCPReconnectTask;
import com.freddy.kulaims.utils.ExecutorServiceFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author FreddyChen
 * @name Netty WebSocket IM Service
 * @date 2020/05/21 16:33
 * @email chenshichao@outlook.com
 * @github https://github.com/FreddyChen
 * @desc 基于Netty实现的WebSocket协议客户端
 */
public class NettyWebSocketIMS implements IMSInterface, NetworkManager.INetworkStateChangedObserver {

    private static final String TAG = NettyWebSocketIMS.class.getSimpleName();
    private Context mContext;
    // ims配置项
    private IMSOptions mIMSOptions;
    // ims连接状态监听器
    private IMSConnectStatusListener mIMSConnectStatusListener;
    // ims消息接收监听器
    private IMSMsgReceivedListener mIMSMsgReceivedListener;
    // ims是否已关闭
    private volatile boolean isClosed = true;
    // 是否正在进行重连
    private volatile boolean isReconnecting = false;
    // 是否已初始化成功
    private boolean initialized = false;
    private Bootstrap bootstrap;
    private Channel channel;
    // ims连接状态
    private volatile IMSConnectStatus imsConnectStatus;
    // 线程池组
    private ExecutorServiceFactory executors;
    // 网络是否可用标识
    private boolean isNetworkAvailable;
    // 是否执行过连接，如果未执行过，在onAvailable()的时候，无需进行重连
    private boolean isExecConnect = false;

    private NettyWebSocketIMS() { }

    public static NettyWebSocketIMS getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        private static final NettyWebSocketIMS INSTANCE = new NettyWebSocketIMS();
    }

    /**
     * 网络可用回调
     */
    @Override
    public void onAvailable() {
        this.isNetworkAvailable = true;
        if(!isExecConnect) {
            return;
        }
        Log.d(TAG, "网络可用，启动ims");
        this.isClosed = false;
        // 网络连接时，自动重连ims
        this.reconnect(false);
    }

    /**
     * 网络不可用回调
     */
    @Override
    public void onUnavailable() {
        this.isNetworkAvailable = false;
        if(!isExecConnect) {
            return;
        }
        Log.d(TAG, "网络不可用，关闭ims");
        this.isClosed = true;
        this.isReconnecting = false;
        // 网络断开时，销毁重连线程组（停止重连任务）
        executors.destroyBossLoopGroup();
        // 关闭channel
        closeChannel();
        // 关闭bootstrap
        closeBootstrap();
    }

    /**
     * 初始化
     * @param context
     * @param options               IMS初始化配置
     * @param connectStatusListener IMS连接状态监听
     * @param msgReceivedListener   IMS消息接收监听
     * @return
     */
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
        // 初始化重连线程池
        executors.initBossLoopGroup();
        // 注册网络连接状态监听
        NetworkManager.getInstance().registerObserver(context, this);
        // 标识ims初始化成功
        initialized = true;
        // 标识ims已打开
        isClosed = false;
        callbackIMSConnectStatus(IMSConnectStatus.Unconnected);
        return true;
    }

    /**
     * 连接
     */
    @Override
    public void connect() {
        if(!initialized) {
            Log.w(TAG, "IMS初始化失败，请查看日志");
            return;
        }
        isExecConnect = true;// 标识已执行过连接
        this.reconnect(true);
    }

    /**
     * 重连
     * @param isFirstConnect 是否首次连接
     */
    @Override
    public void reconnect(boolean isFirstConnect) {
        if (!isFirstConnect) {
            // 非首次重连，代表之前已经进行过重连，延时一段时间再去重连
            try {
                Log.w(TAG, String.format("非首次重连，延时%1$dms再次尝试重连", mIMSOptions.getReconnectInterval()));
                Thread.sleep(mIMSOptions.getReconnectInterval());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!isClosed && !isReconnecting) {
            synchronized (this) {
                if (!isClosed && !isReconnecting) {
                    // 标识正在进行重连
                    setReconnecting(true);
                    // 关闭channel
                    closeChannel();
                    // 开启重连任务
                    executors.execBossTask(new NettyWebSocketReconnectTask(this));
                }
            }
        }
    }

    /**
     * 发送消息
     * @param msg
     */
    @Override
    public void sendMsg(IMSMsg msg) {
        this.sendMsg(msg, null, true);
    }

    /**
     * 发送消息
     * 重载
     * @param msg
     * @param listener 消息发送状态监听器
     */
    @Override
    public void sendMsg(IMSMsg msg, IMSMsgSentStatusListener listener) {
        this.sendMsg(msg, listener, true);
    }

    /**
     * 发送消息
     * 重载
     * @param msg
     * @param isJoinResendManager 是否加入消息重发管理器
     */
    @Override
    public void sendMsg(IMSMsg msg, boolean isJoinResendManager) {
        this.sendMsg(msg, null, isJoinResendManager);
    }

    /**
     * 发送消息
     * 重载
     * @param msg
     * @param listener            消息发送状态监听器
     * @param isJoinResendManager 是否加入消息重发管理器
     */
    @Override
    public void sendMsg(IMSMsg msg, IMSMsgSentStatusListener listener, boolean isJoinResendManager) {
        if(!initialized) {
            Log.w(TAG, "IMS初始化失败，请查看日志");
            return;
        }
    }

    /**
     * 释放资源
     */
    @Override
    public void release() {
        // 关闭channel
        closeChannel();
        // 关闭bootstrap
        closeBootstrap();
        // 标识未进行初始化
        initialized = false;
        // 释放线程池组
        if(executors != null) {
            executors.destroy();
            executors = null;
        }
        // 取消注册网络连接状态监听
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
                .handler(new NettyWebSocketChannelInitializerHandler(this));
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
        if(this.imsConnectStatus == imsConnectStatus) {
            Log.w(TAG, "连接状态与上一次相同，无需执行任何操作");
            return;
        }

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
            case ConnectFailed_IMSClosed:
            case ConnectFailed_ServerListEmpty:
            case ConnectFailed_ServerEmpty:
            case ConnectFailed_ServerIllegitimate:
            case ConnectFailed_NetworkUnavailable:
                int errCode = imsConnectStatus.getErrCode();
                String errMsg = imsConnectStatus.getErrMsg();
                Log.w(TAG, "errCode = " + errCode + "\terrMsg = " + errMsg);
                if (mIMSConnectStatusListener != null) {
                    mIMSConnectStatusListener.onConnectFailed(errCode, errMsg);
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

    /**
     * 标识是否正在进行重连
     * @param isReconnecting
     */
    void setReconnecting(boolean isReconnecting) {
        this.isReconnecting = isReconnecting;
    }
}
