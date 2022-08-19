package com.jagex.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;

/**
 * @author <a href="https://www.rune-server.ee/members/arumat/">Jon Hultin</a> on {8/18/2022}
 * @version 1.0.0
 */
public abstract class NetworkHandler<I> extends ChannelInboundHandlerAdapter {

    private final TypeParameterMatcher matcher;

    private final boolean autoRelease;

    protected NetworkHandler() {
        this(true);
    }

    protected NetworkHandler(boolean autoRelease) {
        matcher = TypeParameterMatcher.find(this, NetworkHandler.class, "I");
        this.autoRelease = autoRelease;
    }

    protected NetworkHandler(Class<? extends I> inboundMessageType) {
        this(inboundMessageType, true);
    }

    protected NetworkHandler(Class<? extends I> inboundMessageType, boolean autoRelease) {
        matcher = TypeParameterMatcher.get(inboundMessageType);
        this.autoRelease = autoRelease;
    }

    public boolean acceptInboundMessage(Object msg) throws Exception {
        return matcher.match(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        boolean release = true;
        try {
            if (acceptInboundMessage(msg)) {
                @SuppressWarnings("unchecked")
                I imsg = (I) msg;
                onRead(ctx, imsg);
            } else {
                release = false;
                ctx.fireChannelRead(msg);
            }
        } finally {
            if (autoRelease && release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    protected abstract void onRead(ChannelHandlerContext ctx, I msg) throws Exception;

}
