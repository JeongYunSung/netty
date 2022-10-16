package com.yunseong.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

fun main(args: Array<String>) {
    val bossGroup: EventLoopGroup = NioEventLoopGroup()
    val workerGroup: EventLoopGroup = NioEventLoopGroup()
    val b = ServerBootstrap()
    b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel::class.java)
        .childHandler(object : ChannelInitializer<SocketChannel>() {
            @Throws(Exception::class)
            override fun initChannel(ch: SocketChannel) {
                val p = ch.pipeline()
                p.addLast(DiscardServerHandler())
            }
        })
    val future = b.bind(8888).sync()
    future.channel().closeFuture().sync()
}

internal class DiscardServerHandler : SimpleChannelInboundHandler<Any?>() {
    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any?) {
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }
}
