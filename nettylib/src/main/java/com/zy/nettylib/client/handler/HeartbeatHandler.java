package com.zy.nettylib.client.handler;

import android.util.Log;

import com.zy.nettylib.client.NettyClient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    private NettyClient mClient;
    private final String TAG = "HeartbeatHandler";

    public HeartbeatHandler(NettyClient mClient) {
        this.mClient = mClient;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            switch (state) {
                case READER_IDLE: {
                    // 规定时间内没收到服务端心跳包响应，进行重连操作
                    mClient.restartConnect();
                    break;
                }

                case WRITER_IDLE: {
                    // 规定时间内没向服务端发送心跳包，即发送一个心跳包
                    if (heartbeatTask == null) {
                        heartbeatTask = new HeartbeatTask(ctx, mClient);
                    }

                    mClient.loopGroup.execWorkTask(heartbeatTask);
                    break;
                }
            }
        }
    }

    private HeartbeatTask heartbeatTask;

    private class HeartbeatTask implements Runnable {

        private ChannelHandlerContext ctx;
        private NettyClient client;

        public HeartbeatTask(ChannelHandlerContext ctx, NettyClient client) {
            this.ctx = ctx;
            this.client = client;
        }

        @Override
        public void run() {
            if (ctx.channel().isActive()) {
                Log.d(TAG, "发送心跳消息，message=" + client.responseListener.getSendHeartMsg());
                mClient.sendData(client.responseListener.getSendHeartMsg());
            }
        }
    }
}