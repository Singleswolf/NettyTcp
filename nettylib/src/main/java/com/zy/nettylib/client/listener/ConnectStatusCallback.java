package com.zy.nettylib.client.listener;

public interface ConnectStatusCallback {
    /**
     * 连接中
     */
    void onConnecting();

    /**
     * 连接成功
     */
    void onConnected(String host, int port);

    /**
     * 连接失败
     */
    void onConnectFailed();
}