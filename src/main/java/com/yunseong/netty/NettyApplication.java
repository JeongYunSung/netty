package com.yunseong.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyApplication {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = (new NioEventLoopGroup());
        EventLoopGroup workerGroup = (new NioEventLoopGroup());
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
            .handler((new LoggingHandler(LogLevel.INFO)))
            .childHandler((new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel ch) {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast((new HttpServerCodec()));
                    p.addLast((new HttpServerInboundHandler()));
                }
        }));
        ChannelFuture future = b.bind(Integer.parseInt(System.getProperty("port", "8080"))).sync();
        future.channel().closeFuture().sync();
    }
}