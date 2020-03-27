package com.zy.nettylib.handler;

import android.util.Log;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<String> {
    private final String TAG = "NettyClientHandler";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Log.d(TAG, "response : " + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        Log.e(TAG, "exceptionCaught: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}