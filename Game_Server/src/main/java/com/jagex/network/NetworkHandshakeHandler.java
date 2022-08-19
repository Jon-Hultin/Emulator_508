package com.jagex.network;

import io.netty.channel.ChannelHandlerContext;

/**
 * <p>
 *     A handler class which dictates server based events based on client requests coordinated by protocol.
 * </p>
 *
 * @author <a href="https://www.rune-server.ee/members/arumat/">Jon Hultin</a> on {8/17/2022}
 * @version 1.0.0
 */
public class NetworkHandshakeHandler extends NetworkHandler<Object> {

    @Override
    protected void onRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

}
