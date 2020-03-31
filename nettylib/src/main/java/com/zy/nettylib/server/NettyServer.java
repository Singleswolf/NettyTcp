package com.zy.nettylib.server;

import android.util.Log;

import com.zy.nettylib.client.Constants;

import java.nio.charset.StandardCharsets;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author: Created by yong on 2020/4/1 12:25.
 */
public class NettyServer {

    private ServerBootstrap mBootstrap;
    private final String TAG = "NettyServer";

    public void startServer(int bindPort) {
        //boss线程监听端口，worker线程负责数据读写
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            //辅助启动类
            mBootstrap = new ServerBootstrap();
            //设置线程池
            mBootstrap.group(boss, worker);

            //设置socket工厂
            mBootstrap.channel(NioServerSocketChannel.class);

            //设置管道工厂
            mBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    //指定分隔符 "\0"
                    ByteBuf delimiter = Unpooled.copiedBuffer(Constants.DELIMITER.getBytes(StandardCharsets.UTF_8));
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1024 * 1024, delimiter));
                    pipeline.addLast("decoder", new StringDecoder());
                    pipeline.addLast("encoder", new StringEncoder());
                    //处理handler
                    pipeline.addLast(ServerReadHandler.class.getSimpleName(), new ServerReadHandler());
                }
            });

            //设置TCP参数
            //1.链接缓冲池的大小（ServerSocketChannel的设置）
            mBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            //维持链接的活跃，清除死链接(SocketChannel的设置)
            mBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            //关闭延迟发送
            mBootstrap.childOption(ChannelOption.TCP_NODELAY, true);

            //绑定端口
            ChannelFuture future = mBootstrap.bind(bindPort).sync();
            Log.d(TAG, "startServer ......");

            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //优雅退出，释放线程池资源
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public void close() {
        if (mBootstrap != null) {
            mBootstrap.group().shutdownGracefully();
            mBootstrap.childGroup().shutdownGracefully();
        }
    }
}
