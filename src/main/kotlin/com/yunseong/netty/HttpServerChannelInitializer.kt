package com.yunseong.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.*
import java.net.InetSocketAddress

class HttpServerChannelInitializer : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val p = ch.pipeline()
        p.addLast(HttpRequestDecoder())
        p.addLast(HttpObjectAggregator(1000))
        p.addLast(HttpResponseEncoder())
        p.addLast(HttpContentCompressor())
        p.addLast(HttpRequestInterceptInboundHandler())
        p.addLast(HttpServerInboundHandler())
    }

    internal class HttpRequestInterceptInboundHandler : SimpleChannelInboundHandler<FullHttpMessage>() {

        override fun channelRead0(ctx: ChannelHandlerContext, msg: FullHttpMessage?) {
            if(msg is HttpRequest) {
                val request = msg as HttpRequest

                request.headers().forEach { (key, value) ->
                }
            }

            if(msg is HttpContent) {
                val httpContent = msg as HttpContent

                val content = httpContent.content()

                println("============== Content ==============")

                println(content.readBytes(content.readableBytes()).toString(Charsets.UTF_8))
            }

            if(msg is LastHttpContent) {
                val trailer = msg as LastHttpContent

                println("============ Last Content ============")

                println(trailer.toString())
            }

            ctx.fireChannelRead(msg)
        }
    }

    internal class HttpServerInboundHandler : ChannelInboundHandlerAdapter() {

        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if(msg is HttpRequest) {
                println("request: ${msg.uri()}")

                val allocator = ctx.alloc()
                val buf = allocator.buffer();

                if (msg.uri().equals("/health")) {
                    val localAddress: InetSocketAddress = ctx.channel()?.localAddress() as InetSocketAddress

                    ctx.write(getResponse(HttpResponseStatus.OK, buf.writeBytes(localAddress.port.toString().toByteArray())))
                }else {
                    ctx.write(getResponse(HttpResponseStatus.NOT_FOUND, buf.writeBytes("".toByteArray())))
                }
            }
        }

        override fun channelReadComplete(ctx: ChannelHandlerContext) {
            ctx.flush()
        }

        private fun getResponse(status: HttpResponseStatus, content: ByteBuf): FullHttpResponse {
            val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content)
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)

            return response
        }
    }
}