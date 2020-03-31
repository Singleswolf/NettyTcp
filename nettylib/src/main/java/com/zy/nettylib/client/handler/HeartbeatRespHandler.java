package com.zy.nettylib.client.handler;

import android.util.Log;

import com.zy.nettylib.client.NettyClient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class HeartbeatRespHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "HeartbeatRespHandler";
    private NettyClient mClient;

    public HeartbeatRespHandler(NettyClient mClient) {
        this.mClient = mClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (((String) msg).equals(mClient.responseListener.getSendHeartMsg())) {
            Log.d(TAG, "收到服务端心跳响应消息，message=" + mClient.responseListener.getSendHeartMsg());
        } else {
            // 消息透传
            ctx.fireChannelRead(msg);
        }
    }
}