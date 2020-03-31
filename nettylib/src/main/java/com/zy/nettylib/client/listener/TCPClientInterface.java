package com.zy.nettylib.client.listener;

/**
 * @author: Created by yong on 2020/3/30 12:37.
 */
public interface TCPClientInterface {
    void startConnect(String host, int port, ConnectStatusCallback connectStatusCallback, OnResponseListener responseListener);

    void restartConnect();

    void sendData(String data);

    void close();
}
