package com.freddy.kulaims.netty.websocket;

import android.util.Log;

import com.freddy.kulaims.config.IMSConnectStatus;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyWebSocketReadHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = NettyWebSocketReadHandler.class.getSimpleName();
    private NettyWebSocketIMS ims;

    NettyWebSocketReadHandler(NettyWebSocketIMS ims) {
        this.ims = ims;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Log.d(TAG, "channelActive() ctx = " + ctx);
    }

    /**
     * ims连接断开回调
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.w(TAG, "channelInactive() ctx = " + ctx);
        closeChannelAndReconnect(ctx);
    }

    /**
     * ims异常回调
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Log.e(TAG, "exceptionCaught() ctx = " + ctx + "\tcause = " + cause);
        closeChannelAndReconnect(ctx);
    }

    /**
     * 收到消息回调
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        Log.d(TAG, "channelRead() ctx = " + ctx + "\tmsg = " + msg);
    }

    /**
     * 关闭channel并重连
     * @param ctx
     */
    private void closeChannelAndReconnect(ChannelHandlerContext ctx) {
        Log.d(TAG, "准备关闭channel并重连");
        Channel channel = ctx.channel();
        if(channel != null) {
            channel.close();
        }
        // 回调连接状态
        ims.callbackIMSConnectStatus(IMSConnectStatus.ConnectFailed);
        // 触发重连
        ims.reconnect(false);
    }
}
