package com.zy.nettylib.client.listener;

/**
 * @author: Created by yong on 2020/3/30 14:38.
 */
public interface OnResponseListener {
    void onResponse(String response);
    String getSendHeartMsg();
    boolean isNetworkAvailable();
}
