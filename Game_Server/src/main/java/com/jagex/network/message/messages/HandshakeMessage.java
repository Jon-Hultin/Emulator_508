package com.jagex.network.message.messages;

import com.jagex.network.message.Message;
import com.jagex.network.metadata.HandshakeRequest;

import io.netty.channel.ChannelHandlerContext;

import java.util.Optional;

/**
 * @author <a href="https://www.rune-server.ee/members/arumat/">Jon Hultin</a> on {8/17/2022}
 * @version 1.0.0
 */
public class HandshakeMessage extends Message {

    public HandshakeMessage(int opcode) {
        super(opcode);
    }

    @Override
    public void handleRequest(ChannelHandlerContext ctx) {
        var optionalRequest = Optional.of(HandshakeRequest.ofOpcode(opcode));
        if (optionalRequest.isEmpty()) {
            ctx.deregister();
            return;
        }
        var request = optionalRequest.get();
        switch(request) {
            case LOGIN_REQUEST -> {
                System.out.println("Login Request");
            }

            case JS5_UPDATE_REQUEST -> {
                System.out.println("JS5 Update Request");
            }

            case JAGGRAB_REQUEST -> {
                System.out.println("JAGGRAB Request");
            }
        }
    }

}
