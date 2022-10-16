package com.yunseong.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.nio.charset.Charset

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
                p.addLast(EchoServerHandler())
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

internal class EchoServerHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        val readMessage: String = (msg as ByteBuf).toString(Charset.defaultCharset())

        println("수신한 문자열 [$readMessage]")

        ctx?.write(msg)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        ctx?.flush()
    }
}
