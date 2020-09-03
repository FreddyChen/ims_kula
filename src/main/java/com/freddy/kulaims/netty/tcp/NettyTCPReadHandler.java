package com.freddy.kulaims.netty.tcp;

import android.util.Log;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyTCPReadHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = NettyTCPReadHandler.class.getSimpleName();
    private NettyTCPIMS ims;

    NettyTCPReadHandler(NettyTCPIMS ims) {
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
        Channel channel = ctx.channel();
        if(channel != null) {
            channel.close();
        }
        // 触发重连
        ims.reconnect(false);
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
        Channel channel = ctx.channel();
        if(channel != null) {
            channel.close();
        }
        // 触发重连
        ims.reconnect(false);
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
}
