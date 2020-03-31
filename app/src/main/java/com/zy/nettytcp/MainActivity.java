package com.zy.nettytcp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.zy.nettylib.client.NettyClient;
import com.zy.nettylib.client.listener.ConnectStatusCallback;
import com.zy.nettylib.client.listener.OnResponseListener;

public class MainActivity extends AppCompatActivity implements ConnectStatusCallback, OnResponseListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        NettyClient.getInstance().connect("192.168.1.118", 8888);

    }

    public void start(View view) {
        NettyClient.getInstance().startConnect("2i6564z935.wicp.vip", 50138, this, this);
    }

    public void restart(View view) {
        NettyClient.getInstance().restartConnect();
    }

    public void close(View view) {
        NettyClient.getInstance().close();
    }

    public void send(View view) {
        String data = "{\"version\":\"1.0\",\"type\":\"login_req\",\"sn\":\"30:09:F9:11:20:B1\",\"seq\":1,\"data\":{\"ver\":1,\"sn\":\"30:09:F9:11:20:B1\"}}";
        String data2 = "{\"version\":\"1.0\",\"type\":\"rsync_person_req\",\"sn\":\"30:09:F9:11:20:B1\",\"seq\":1,\"data\":{\"person_type\":0,\"person_id\":\"620\"}}";
        Log.d(TAG, "sendData: " + data2);
        NettyClient.getInstance().sendData(data2);
    }

    @Override
    public void onConnecting() {
        Log.d(TAG, "onConnecting");
    }

    @Override
    public void onConnected(String host, int port) {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectFailed() {
        Log.d(TAG, "onConnectFailed");
    }

    @Override
    public void onResponse(String response) {
        Log.d(TAG, "onResponse: " + response);
    }

    @Override
    public String getSendHeartMsg() {
        String heartMsg = "{\"data\":{\"device_app_version\":\"2.0.16\",\"device_elapsed_realtime\":67639445,\"device_ip_address\":\"192.168.5.103\",\"device_register_time\":0,\"device_rom_available_size\":\"8541\",\"device_rom_size\":\"10989\",\"device_sn\":\"30:09:F9:11:20:B1\",\"device_system_version\":\"FaceOS_QX3,27,8.1.0\",\"watchdog_version\":\"1.0\"},\"seq\":1,\"sn\":\"30:09:F9:11:20:B1\",\"type\":\"heart_beat_req\",\"version\":\"1.0\"}";
        return heartMsg;
    }

    @Override
    public boolean isNetworkAvailable() {
        return true;
    }
}
