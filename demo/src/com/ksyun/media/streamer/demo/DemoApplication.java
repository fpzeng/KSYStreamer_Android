package com.ksyun.media.streamer.demo;

import com.ksyun.media.streamer.util.device.DeviceInfoTools;

import android.app.Application;

/**
 * demo for deviceTools
 * 建议在app加载时对DeviceInfoTools进行初始化，以便最快拿到设备信息
 */

public class DemoApplication extends Application {
    @Override
    public void onCreate() {

        super.onCreate();
        //初始化本地存储，若本地无信息或者信息已经过期，会向服务器发起请求
        DeviceInfoTools.getInstance().init(this);
    }
}
