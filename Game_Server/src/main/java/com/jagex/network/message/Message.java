package com.jagex.network.message;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author <a href="https://www.rune-server.ee/members/arumat/">Jon Hultin</a> on {8/17/2022}
 * @version 1.0.0
 */
public abstract class Message {

    protected final int opcode;

    public Message(int opcode) {
        this.opcode = opcode;
    }

    public int opcode() {
        return opcode;
    }

    public abstract void handleRequest(ChannelHandlerContext ctx);
}
