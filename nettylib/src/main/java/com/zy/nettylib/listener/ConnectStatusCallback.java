package com.zy.nettylib.listener;

public interface ConnectStatusCallback {
    /**
     * 连接中
     */
    void onConnecting();

    /**
     * 连接成功
     */
    void onConnected();

    /**
     * 连接失败
     */
    void onConnectFailed();
}