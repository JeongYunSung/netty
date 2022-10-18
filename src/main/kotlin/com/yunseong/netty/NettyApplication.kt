package com.yunseong.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.bootstrap.ServerBootstrapConfig
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.local.LocalAddress
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import java.net.InetSocketAddress
import java.nio.charset.Charset

fun main(args: Array<String>) {
    val bossGroup: EventLoopGroup = NioEventLoopGroup()
    val workerGroup: EventLoopGroup = NioEventLoopGroup()
    val b = ServerBootstrap()
    b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel::class.java)
        .handler(LoggingHandler(LogLevel.INFO))
        .childHandler(object : ChannelInitializer<SocketChannel>() {
            @Throws(Exception::class)
            override fun initChannel(ch: SocketChannel) {
                val p = ch.pipeline()
                p.addLast(HttpServerCodec())
                p.addLast(HttpServerInboundHandler())
            }
        })
    val future = b.bind(System.getProperty("port").toInt()).sync()
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

internal class HttpServerInboundHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if(msg is HttpRequest) {
            println("request: ${msg.uri()}")
            if (msg.uri().equals("/health")) {
                val localAddress: InetSocketAddress = ctx?.channel()?.localAddress() as InetSocketAddress

                ctx.write(getResponse(HttpResponseStatus.OK, Unpooled.wrappedBuffer(localAddress.port.toString().toByteArray())))
            }else {
                ctx?.write(getResponse(HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer("".toByteArray())))
            }
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        ctx?.flush()
    }

    private fun getResponse(status: HttpResponseStatus, content: ByteBuf): FullHttpResponse {
        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content)
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)

        return response
    }
}