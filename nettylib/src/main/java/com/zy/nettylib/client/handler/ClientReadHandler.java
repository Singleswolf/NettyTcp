package com.zy.nettylib.client.handler;

import android.util.Log;

import com.zy.nettylib.client.NettyClient;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientReadHandler extends SimpleChannelInboundHandler<String> {
    private final String TAG = "ClientReadHandler";
    private final NettyClient client;

    public ClientReadHandler(NettyClient client) {
        this.client = client;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.w(TAG, "ClientReadHandler channelInactive()");
        Channel channel = ctx.channel();
        if (channel != null) {
            channel.close();
            ctx.close();
        }
        // 触发重连
        client.restartConnect();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        if (client.responseListener != null) {
            client.responseListener.onResponse(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.w(TAG, "ClientReadHandler exceptionCaught: " + cause.getMessage());
        ctx.fireExceptionCaught(cause);
        Channel channel = ctx.channel();
        if (channel != null) {
            channel.close();
            ctx.close();
        }

        // 触发重连
        client.restartConnect();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}