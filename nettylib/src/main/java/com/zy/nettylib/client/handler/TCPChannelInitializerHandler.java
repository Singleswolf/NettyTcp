package com.zy.nettylib.client.handler;

import com.zy.nettylib.client.Constants;
import com.zy.nettylib.client.NettyClient;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author: Created by yong on 2020/3/27 21:12.
 */
public class TCPChannelInitializerHandler extends ChannelInitializer<SocketChannel> {
    private final NettyClient client;

    public TCPChannelInitializerHandler(NettyClient nettyClient) {
        this.client = nettyClient;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //指定分隔符 "\0"
        ByteBuf delimiter = Unpooled.copiedBuffer(Constants.DELIMITER.getBytes(StandardCharsets.UTF_8));
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1024 * 1024, delimiter));
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());
        // 心跳消息响应处理handler
        pipeline.addLast(HeartbeatRespHandler.class.getSimpleName(), new HeartbeatRespHandler(client));
        pipeline.addLast(ClientReadHandler.class.getSimpleName(), new ClientReadHandler(client));
    }
}
