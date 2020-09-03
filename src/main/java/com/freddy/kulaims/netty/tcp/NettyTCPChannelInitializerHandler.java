package com.freddy.kulaims.netty.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public class NettyTCPChannelInitializerHandler extends ChannelInitializer<Channel> {

    private NettyTCPIMS ims;

    NettyTCPChannelInitializerHandler(NettyTCPIMS ims) {
        this.ims = ims;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new NettyTCPReadHandler(ims));
    }
}
