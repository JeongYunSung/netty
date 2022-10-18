package com.yunseong.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class HttpServerInboundHandler extends ChannelInboundHandlerAdapter {
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            String var3 = "request: " + ((HttpRequest) msg).uri();
            System.out.println(var3);
            ByteBuf var9;
            HttpResponseStatus var10002;
            byte[] var10003;
            if (((HttpRequest) msg).uri().equals("/health")) {
                Channel var10000 = ctx.channel();
                SocketAddress var8 = var10000 != null ? var10000.localAddress() : null;
                if (var8 == null) {
                    throw new NullPointerException("null cannot be cast to non-null type java.net.InetSocketAddress");
                }

                InetSocketAddress localAddress = (InetSocketAddress) var8;
                var10002 = HttpResponseStatus.OK;
                String var4 = String.valueOf(localAddress.getPort());
                var10003 = var4.getBytes(StandardCharsets.UTF_8);
            } else {
                var10002 = HttpResponseStatus.NOT_FOUND;
                var3 = "";
                var10003 = var3.getBytes(StandardCharsets.UTF_8);
            }
            var9 = Unpooled.wrappedBuffer(var10003);
            ctx.write(this.getResponse(var10002, var9));
        }
    }

    public void channelReadComplete(ChannelHandlerContext ctx) {
        if (ctx != null) {
            ctx.flush();
        }
    }

    private FullHttpResponse getResponse(HttpResponseStatus status, ByteBuf content) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        return response;
    }
}