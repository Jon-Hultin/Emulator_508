package com.jagex;

import com.jagex.network.NetworkChannelInitializer;
import com.jagex.network.NetworkConstants;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;

/**
 * @author <a href="https://www.rune-server.ee/members/arumat/">Jon Hultin</a> on {$DATE}
 * @version 1.0.0
 */
public class GameServer {

    public void start() {
        var bootstrap = new ServerBootstrap();
        var eventGroup = new NioEventLoopGroup();

        bootstrap.group(eventGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new NetworkChannelInitializer());
        bootstrap.bind(NetworkConstants.GAME_PORT).syncUninterruptibly();
    }

    public static void main(String[] args) throws IOException {
        var server = new GameServer();
        server.start();
    }

}