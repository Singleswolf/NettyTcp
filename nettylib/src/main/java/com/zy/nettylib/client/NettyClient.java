package com.zy.nettylib.client;

import android.util.Log;

import com.zy.nettylib.client.handler.HeartbeatHandler;
import com.zy.nettylib.client.handler.ClientReadHandler;
import com.zy.nettylib.client.handler.TCPChannelInitializerHandler;
import com.zy.nettylib.client.listener.ConnectStatusCallback;
import com.zy.nettylib.client.listener.OnResponseListener;
import com.zy.nettylib.client.listener.TCPClientInterface;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author : Created by yong on 2020/3/27 12:02.
 */
public class NettyClient implements TCPClientInterface {
    private final String TAG = "NettyClient";
    private Channel mChannel;

    private static NettyClient instance;
    private String host;
    private int port;
    private ConnectStatusCallback connectStatusCallback;
    public OnResponseListener responseListener;
    private boolean isClosed;
    private Bootstrap mBootstrap;
    private boolean isReConnecting;

    public final int RECONNECT_INTERVAL = 3 * 1000;
    public final int CONNECT_STATE_CONNECTING = 0;
    public final int CONNECT_STATE_SUCCESS = 1;
    public final int CONNECT_STATE_FAILED = -1;
    private long heartbeatInterval = 3 * 1000;
    private long reconnectCount = 3;
    public ExecutorServiceFactory loopGroup;

    private NettyClient() {
    }

    public static NettyClient getInstance() {
        if (instance == null) {
            instance = new NettyClient();
        }
        return instance;
    }

    private void initBootstrap() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        mBootstrap = new Bootstrap();
        mBootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000 * 10)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new TCPChannelInitializerHandler(this));
    }

    @Override
    public void startConnect(String host, int port, ConnectStatusCallback connectStatusCallback, OnResponseListener responseListener) {
        close();
        isClosed = false;
        this.host = host;
        this.port = port;
        this.connectStatusCallback = connectStatusCallback;
        this.responseListener = responseListener;
        loopGroup = new ExecutorServiceFactory();
        loopGroup.initBossLoopGroup();// 初始化重连线程组
        reConnect(true);
    }

    private void reConnect(boolean isFirst) {
        if (!isFirst) {
            try {
                Thread.sleep(RECONNECT_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!isClosed && !isReConnecting) {
            synchronized (this) {
                if (!isClosed && !isReConnecting) {
                    // 标识正在进行重连
                    isReConnecting = true;
                    // 回调ims连接状态
                    if (connectStatusCallback != null) {
                        connectStatusCallback.onConnecting();
                    }
                    // 先关闭channel
                    closeChannel();
                    // 执行重连任务
                    loopGroup.execBossTask(new ResetConnectRunnable(isFirst));
                }
            }
        }
    }

    private void closeChannel() {
        try {
            if (mChannel != null) {
                try {
                    removeHandler(HeartbeatHandler.class.getSimpleName());
                    removeHandler(ClientReadHandler.class.getSimpleName());
                    removeHandler(IdleStateHandler.class.getSimpleName());
                } finally {
                    try {
                        mChannel.close();
                    } catch (Exception ex) {
                    }
                    try {
                        mChannel.eventLoop().shutdownGracefully();
                    } catch (Exception ex) {
                    }
                    mChannel = null;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "关闭channel出错，reason:" + ex.getMessage());
        }
    }

    private void removeHandler(String handlerName) {
        try {
            if (mChannel.pipeline().get(handlerName) != null) {
                mChannel.pipeline().remove(handlerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "移除handler失败，handlerName=" + handlerName);
        }
    }

    /**
     * 添加心跳消息管理handler
     */
    public void addHeartbeatHandler() {
        if (mChannel == null || !mChannel.isActive() || mChannel.pipeline() == null) {
            return;
        }

        try {
            // 之前存在的读写超时handler，先移除掉，再重新添加
            if (mChannel.pipeline().get(IdleStateHandler.class.getSimpleName()) != null) {
                mChannel.pipeline().remove(IdleStateHandler.class.getSimpleName());
            }
            // 3次心跳没响应，代表连接已断开
            mChannel.pipeline().addFirst(IdleStateHandler.class.getSimpleName(), new IdleStateHandler(
                    heartbeatInterval * 3, heartbeatInterval, 0, TimeUnit.MILLISECONDS));

            // 重新添加HeartbeatHandler
            if (mChannel.pipeline().get(HeartbeatHandler.class.getSimpleName()) != null) {
                mChannel.pipeline().remove(HeartbeatHandler.class.getSimpleName());
            }
            if (mChannel.pipeline().get(ClientReadHandler.class.getSimpleName()) != null) {
                mChannel.pipeline().addBefore(ClientReadHandler.class.getSimpleName(), HeartbeatHandler.class.getSimpleName(),
                        new HeartbeatHandler(this));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "添加心跳消息管理handler失败，reason：" + e.getMessage());
        }
    }

    @Override
    public void restartConnect() {
        reConnect(false);
    }

    @Override
    public void sendData(String data) {
        if (mChannel != null && mChannel.isOpen()) {
            try {
                mChannel.writeAndFlush(data + Constants.DELIMITER).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (future.isSuccess()) {
                            Log.e(TAG, "send isSuccess");
                        } else {
                            Log.e(TAG, "send failed");
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "sendData failed : " + e.getMessage());
            }
        } else {
            Log.d(TAG, "sendData failed,not connect or close");
        }
    }

    @Override
    public void close() {
        if (isClosed) {
            return;
        }

        isClosed = true;

        // 关闭channel
        try {
            closeChannel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 关闭bootstrap
        try {
            if (mBootstrap != null) {
                mBootstrap.group().shutdownGracefully();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            // 释放线程池
            if (loopGroup != null) {
                loopGroup.destroy();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            isReConnecting = false;
            mChannel = null;
            mBootstrap = null;
        }
    }

    private class ResetConnectRunnable implements Runnable {
        private boolean isFirst;

        public ResetConnectRunnable(boolean isFirst) {
            this.isFirst = isFirst;
        }

        @Override
        public void run() {
            if (!isFirst) {
                if (connectStatusCallback != null) {
                    connectStatusCallback.onConnectFailed();
                }
            }

            try {
                // 重连时，释放工作线程组，也就是停止心跳
                loopGroup.destroyWorkLoopGroup();

                while (!isClosed) {
                    if (!responseListener.isNetworkAvailable()) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    // 网络可用才进行连接
                    try {
                        // 先释放EventLoop线程组
                        if (mBootstrap != null) {
                            mBootstrap.group().shutdownGracefully();
                        }
                    } finally {
                        mBootstrap = null;
                    }
                    // 初始化bootstrap
                    initBootstrap();

                    if (connect() == CONNECT_STATE_SUCCESS){
                        addHeartbeatHandler();
                        break;
                    } else {
                        if (connectStatusCallback != null) {
                            connectStatusCallback.onConnectFailed();
                        }
                    }
                    return;
                }
            } finally {
                // 标识重连任务停止
                isReConnecting = false;
            }
        }

        private int connect() {
            for (long i = 1; i <= reconnectCount; i++) {
                // 如果ims已关闭，或网络不可用，直接回调连接状态，不再进行连接
                if (isClosed || !responseListener.isNetworkAvailable()) {
                    if (connectStatusCallback != null) {
                        connectStatusCallback.onConnectFailed();
                        return CONNECT_STATE_FAILED;
                    }
                }
                if (connectStatusCallback != null) {
                    connectStatusCallback.onConnecting();
                }
                Log.w(TAG, "正在进行第" + i + "次连接");
                try {
                    connectServer();
                    // channel不为空，即认为连接已成功
                    if (mChannel != null) {
                        if (connectStatusCallback != null) {
                            connectStatusCallback.onConnected(host, port);
                        }
                        return CONNECT_STATE_SUCCESS;
                    } else {
                        // 连接失败，则线程休眠n * 重连间隔时长
                        Thread.sleep(i * RECONNECT_INTERVAL);
                    }
                } catch (InterruptedException e) {
                    close();
                    break;// 线程被中断，则强制关闭
                }
            }
            return CONNECT_STATE_FAILED;
        }
    }

    private void connectServer() {
        try {
            mChannel = mBootstrap.connect(host, port).sync().channel();
        } catch (Exception e) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            mChannel = null;
        }
    }
}
