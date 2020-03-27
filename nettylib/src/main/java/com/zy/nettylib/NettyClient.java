package com.zy.nettylib;

import android.util.Log;

import java.nio.charset.StandardCharsets;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author : Created by yong on 2020/3/27 12:02.
 */
public class NettyClient {
    private final String TAG = "NettyClient";
    private Channel mChannel;

    private static NettyClient instance;

    private NettyClient() {
    }

    public static NettyClient getInstance() {
        if (instance == null) {
            instance = new NettyClient();
        }
        return instance;
    }

    public void connect(String host, int port) {
        final NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        //指定分隔符 "\0"
                        ByteBuf delimiter = Unpooled.copiedBuffer(Constants.DELIMITER.getBytes(StandardCharsets.UTF_8));
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1024 * 1024, delimiter));
                        pipeline.addLast("decoder", new StringDecoder());
                        pipeline.addLast("encoder", new StringEncoder());
                        pipeline.addLast("handler", new NettyClientHandler());
                    }
                });
        //发起异步连接操作
        bootstrap.connect(host, port).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    Log.e(TAG, "connect isSuccess");
                    // 连接成功
                    mChannel = future.channel();
                } else {
                    Log.e(TAG, "connect failed");
                    // 这里一定要关闭，不然一直重试会引发OOM
                    future.channel().close();
                    group.shutdownGracefully();
                }
            }
        });
    }

    public void sendData(String str) {
        if (mChannel != null && mChannel.isOpen()) {
            mChannel.writeAndFlush(str + Constants.DELIMITER).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        Log.e(TAG, "send isSuccess");
                    } else {
                        Log.e(TAG, "send failed");
                    }
                }
            });
        } else {
            Log.d(TAG, "connect is close");
        }
    }
}
