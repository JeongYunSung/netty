package com.yunseong.netty

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import java.nio.charset.Charset

fun client() {
    val group : EventLoopGroup = NioEventLoopGroup()

    val b = Bootstrap()
    b.group(group)
        .channel(NioSocketChannel::class.java)
        .handler(object : ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
            @Throws(Exception::class)
            override fun initChannel(ch: io.netty.channel.socket.SocketChannel) {
                val p = ch.pipeline()
                p.addLast(EchoClientHandler())
            }
        })

    val future : ChannelFuture = b.connect("localhost", 8888).sync()

    future.channel().closeFuture().sync()
}

internal class EchoClientHandler : ChannelInboundHandlerAdapter() {

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
        ctx?.close()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
         val sendMessage = "Hello, Netty!"

        val byteBuf : ByteBuf = Unpooled.buffer()
        byteBuf.writeBytes(sendMessage.toByteArray())

        val builder = StringBuilder()

        builder.append("전송한 문자열 [")
        builder.append(sendMessage)
        builder.append("]")

        println(builder)

        ctx.writeAndFlush(byteBuf)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val readMessage: String = (msg as ByteBuf).toString(Charset.defaultCharset())

        val builder = StringBuilder()

        builder.append("수신한 문자열 [")
        builder.append(readMessage)
        builder.append("]")

        println(builder)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        ctx?.close()
    }
}