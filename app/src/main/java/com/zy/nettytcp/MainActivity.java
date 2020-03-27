package com.zy.nettytcp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.zy.nettylib.NettyClient;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NettyClient.getInstance().connect("2i6564z935.wicp.vip", 50138);
//        NettyClient.getInstance().connect("192.168.1.118", 8888);

    }

    public void btn(View view) {
//        request RequestParameter{version='1.0', type='login_req', seq=1, sn='30:09:F9:11:20:B1', data=com.aiwinn.tcplibrary.tcp.parameter.LoginParameter@9f25d5b}
        String data = "{\"version\":\"1.0\",\"type\":\"login_req\",\"sn\":\"30:09:F9:11:20:B1\",\"seq\":1,\"data\":{\"ver\":1,\"sn\":\"30:09:F9:11:20:B1\"}}";
        String data2 = "{\"version\":\"1.0\",\"type\":\"rsync_person_req\",\"sn\":\"30:09:F9:11:20:B1\",\"seq\":1,\"data\":{\"person_type\":0,\"person_id\":\"620\"}}";
        Log.d(TAG, "sendData: " + data2);
        NettyClient.getInstance().sendData(data2);
    }
}
