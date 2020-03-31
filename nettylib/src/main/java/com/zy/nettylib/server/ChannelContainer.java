package com.zy.nettylib.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;

/**
 * @author: Created by yong on 2020/4/1 14:38.
 */
public class ChannelContainer {
    private ChannelContainer() {

    }

    private static final ChannelContainer INSTANCE = new ChannelContainer();

    public static ChannelContainer getInstance() {
        return INSTANCE;
    }

    private final Map<String, Channel> CHANNELS = new ConcurrentHashMap<>();

    public void saveChannel(Channel channel) {
        if (channel == null) {
            return;
        }
        CHANNELS.put(channel.id().toString(), channel);
    }

    public void removeChannelIfConnectNoActive(Channel channel) {
        if (channel == null) {
            return;
        }

        String channelId = channel.id().toString();
        if (CHANNELS.containsKey(channelId) && !CHANNELS.get(channelId).isActive()) {
            CHANNELS.remove(channelId);
        }
    }
}
