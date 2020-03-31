package com.zy.nettylib.server;

import android.util.Log;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author: Created by yong on 2020/4/1 12:45.
 */
class ServerReadHandler extends ChannelInboundHandlerAdapter {
    private static final String TAG = "ServerReadHandler";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Log.d(TAG, "channelActive: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.e(TAG, "channelInactive: " + ctx.channel().remoteAddress());
        // 用户断开连接后，移除channel
        ChannelContainer.getInstance().removeChannelIfConnectNoActive(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Log.e(TAG, "exceptionCaught: " + cause.getMessage());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        Log.d(TAG, "userEventTriggered");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String str = (String) msg;
        Log.d(TAG, "channelRead: " + str);
    }
}
