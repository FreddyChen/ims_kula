package com.freddy.kulaims.netty.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public class NettyWebSocketChannelInitializerHandler extends ChannelInitializer<Channel> {

    private NettyWebSocketIMS ims;

    NettyWebSocketChannelInitializerHandler(NettyWebSocketIMS ims) {
        this.ims = ims;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new NettyWebSocketReadHandler(ims));
    }
}
