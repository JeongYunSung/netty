package com.yunseong.netty

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import java.lang.StringBuilder
import java.nio.charset.Charset

fun main(args: Array<String>) {
    var group : EventLoopGroup = NioEventLoopGroup()

    var b : Bootstrap = Bootstrap()
    b.group(group)
        .channel(NioSocketChannel::class.java)
        .handler(object : ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
            @Throws(Exception::class)
            override fun initChannel(ch: io.netty.channel.socket.SocketChannel) {
                val p = ch.pipeline()
                p.addLast(EchoClientHandler())
            }
        })

    var future : ChannelFuture = b.connect("localhost", 8888).sync()

    future.channel().closeFuture().sync()
}

internal class EchoClientHandler : ChannelInboundHandlerAdapter() {

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
        ctx?.close()
    }

    override fun channelActive(ctx: ChannelHandlerContext?) {
         var sendMessage = "Hello, Netty!"

        var byteBuf : ByteBuf = Unpooled.buffer()
        byteBuf.writeBytes(sendMessage.toByteArray())

        var builder = StringBuilder()

        builder.append("전송한 문자열 [")
        builder.append(sendMessage)
        builder.append("]")

        println(builder)

        ctx?.writeAndFlush(byteBuf)
    }

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        var readMessage: String = (msg as ByteBuf).toString(Charset.defaultCharset())

        var builder = StringBuilder()

        builder.append("수신한 문자열 [")
        builder.append(readMessage)
        builder.append("]")

        println(builder)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        ctx?.close()
    }
}