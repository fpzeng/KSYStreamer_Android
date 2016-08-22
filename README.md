# 金山云直播推流Android SDK使用说明

KSY Streamer Android SDK是金山云推出的 Android 平台上使用的软件开发工具包(SDK), 负责视频直播的采集、预处理、编码和推流。  
## 一. 功能特点

* [x] [支持软编和硬编](https://github.com/ksvc/KSYStreamer_Android/wiki/%E6%8E%A8%E6%B5%81%E5%88%9D%E5%A7%8B%E5%8C%96%E5%8F%82%E6%95%B0#%E7%BC%96%E7%A0%81%E5%99%A8%E9%80%89%E6%8B%A9)
* [x] [网络自适应](https://github.com/ksvc/KSYStreamer_Android/wiki/%E6%8E%A8%E6%B5%81%E5%88%9D%E5%A7%8B%E5%8C%96%E5%8F%82%E6%95%B0#%E8%A7%86%E9%A2%91%E7%A0%81%E7%8E%87%E5%8F%8A%E7%A0%81%E7%8E%87%E8%87%AA%E9%80%82%E5%BA%94)， 可根据实际网络情况动态调整目标码率，保证流畅性
* [x] 音频编码：AAC
* [x] 视频编码：H.264
* [x] 推流协议：RTMP
* [x] [视频分辨率](https://github.com/ksvc/KSYStreamer_Android/wiki/%E6%8E%A8%E6%B5%81%E5%88%9D%E5%A7%8B%E5%8C%96%E5%8F%82%E6%95%B0#%E8%A7%86%E9%A2%91%E5%88%86%E8%BE%A8%E7%8E%87)：支持360P, 480P, 540P和720P
* [x] 音视频目标码率：可设
* [x] 支持固定横屏或固定竖屏推流
* [x] 支持前、后置摄像头动态切换
* [x] [前置摄像头镜像功能](https://github.com/ksvc/KSYStreamer_Android/wiki/%E9%95%9C%E5%83%8F)
* [x] 闪光灯：开/关
* [x] [内置美颜功能](https://github.com/ksvc/KSYStreamer_Android/wiki/%E5%86%85%E7%BD%AE%E7%BE%8E%E9%A2%9C)
* [x] [自定义美颜接口](https://github.com/ksvc/KSYStreamer_Android/wiki/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%BB%A4%E9%95%9C)
* [x] [美声](https://github.com/ksvc/KSYStreamer_Android/wiki/%E7%BE%8E%E5%A3%B0)
* [x] [背景音乐功能, 支持本地mp3, aac等格式](https://github.com/ksvc/KSYStreamer_Android/wiki/%E6%B7%B7%E9%9F%B3)
* [x] [支持手动指定自动对焦测光区域](https://github.com/ksvc/KSYStreamer_Android/wiki/%E6%89%8B%E5%8A%A8%E5%AF%B9%E7%84%A6)
* [x] [支持图片及时间水印](https://github.com/ksvc/KSYStreamer_Android/wiki/%E6%B0%B4%E5%8D%B0)
* [x] 耳返
* [x] [画中画](https://github.com/ksvc/KSYStreamer_Android/wiki/%E7%94%BB%E4%B8%AD%E7%94%BB)
* [x] 连麦(new)

## 二. 运行环境

* 最低支持版本为Android 4.0 (API level 15)
* 支持的cpu架构：armv7, arm64, x86

软硬编部分功能版本需求列表:

|           |软编         |硬编         |
|-----------|------------|------------|
|基础推流   |4.0 (15)   |4.3 (18)   |
|网络自适应  |4.0 (15)   |4.4 (19)   |
  

## 三. 开发指南

[Wiki](https://github.com/ksvc/KSYStreamer_Android/wiki)

## 四. 版本迭代
[最新及历史版本](https://github.com/ksvc/KSYStreamer_Android/releases)

## 五. 快速集成

本章节提供一个快速集成金山云推流SDK基础功能的示例。更详细的文档地址：[https://github.com/ksvc/KSYStreamer_Android/wiki](https://github.com/ksvc/KSYStreamer_Android/wiki)  
具体可以参考demo工程中的相应文件。

### 5.1 下载工程

#### 5.1.1 github下载
从github下载SDK及demo工程：  <https://github.com/ksvc/KSYStreamer_Android.git>

#### 5.1.2 oschina下载
<http://git.oschina.net/ksvc/KSYStreamer_Android>
对于部分地方访问github比较慢的情况，可以从oschina clone，获取的库内容和github一致。

```
$ git clone https://git.oschina.net/ksvc/KSYStreamer_Android.git
```
### 5.2 工程目录结构

- demo: 示例工程，演示本SDK主要接口功能的使用
- doc: SDK说明文档
- libs: 集成SDK需要的所有库文件
    - libs/[armeabi-v7a|arm64-v8a|x86]: 各平台的so库
    - libs/ksylive3.0.jar: 推流SDK jar包
    - libs/libksystat.jar: 金山云统计模块

### 5.3 配置项目

引入目标库, 将libs目录下的库文件引入到目标工程中并添加依赖。

可参考下述配置方式（以Android Studio为例）：
- 将libs目录copy到目标工程的根目录下；
- 修改目标工程的build.gradle文件，配置jniLibs路径：
````gradle
    sourceSets {
        main {
            ...
            jniLibs.srcDir 'libs'
        }
        ...
    }
````
- 修改proguard文件，需要保持com.ksy.recordlib下的所有类：
````
-keep class com.ksy.recordlib.** { *;}
````
- 在AndroidManifest.xml文件中申请相应权限
````xml
<!-- 使用权限 -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_SINTERNETWIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.FLASHLIGHT" />
<uses-permission android:name="android.permission.VIBRATE" />
<!-- 硬件特性 -->
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
````

### 5.4 简单推流示例

具体可参考demo工程中的`com.ksy.recordlib.demo.CameraActivity`类

- 在布局文件中加入预览View
````xml
<com.ksy.recordlib.service.view.CameraGLSurfaceView
    android:id="@+id/camera_preview"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_alignParentBottom="true"
    android:layout_alignParentTop="true" />
````
- 初始化GLSurfaceView
````java
GLSurfaceView mCameraPreview = (GLSurfaceView)findViewById(R.id.camera_preview)
````
- 创建并配置KSYStreamerConfig, KSYStreamerConfig采用了Builder模式。
推流过程中不可动态改变的参数需要在创建该类的对象时指定。
````java
KSYStreamerConfig.Builder builder = new KSYStreamerConfig.Builder();
// 设置推流url（需要向相关人员申请，测试地址并不稳定！）
builder.setmUrl("rtmp://test.uplive.ksyun.com/live/{streamName}");
/**
 * 设置推流分辨率，支持以下值：
 * RecorderConstants.VIDEO_RESOLUTION_360P
 * RecorderConstants.VIDEO_RESOLUTION_480P
 * RecorderConstants.VIDEO_RESOLUTION_540P
 * RecorderConstants.VIDEO_RESOLUTION_720P
 */
builder.setVideoResolution(RecorderConstants.VIDEO_RESOLUTION_360P);
// 设置视频帧率
builder.setFrameRate(15);
// 设置视频码率(分别为最大、最小、初始码率, 单位为kbps)
builder.setMaxAverageVideoBitrate(800);
builder.setMinAverageVideoBitrate(200);
builder.setInitAverageVideoBitrate(500);
// 设置音频码率(单位为kbps)
builder.setAudioBitrate(48);
// 设置音频采样率(硬编模式下暂时无效)
builder.setSampleAudioRateInHz(44100);
/**
 * 设置编码模式(软编、硬编), 支持的类型：
 * KSYStreamerConfig.ENCODE_METHOD.SOFTWARE
 * KSYStreamerConfig.ENCODE_METHOD.HARDWARE
 */
builder.setEncodeMethod(KSYStreamerConfig.ENCODE_METHOD.SOFTWARE);
// 设置是否采用横屏模式
builder.setDefaultLandscape(false);
// 开启推流统计功能
builder.setEnableStreamStatModule(true);
// 创建KSYStreamerConfig对象
KSYStreamerConfig config = builder.build();
````
- 创建推流事件监听，可以收到推流过程中的异步事件。事件监听分两种：
1:StreamStatusEventHandler.OnStatusInfoListener  可以接收到状态通知，APP可以在收到对应的通知时提示用户
2:StreamStatusEventHandler.OnStatusErrorListener 可以接收到错误通知，一般是发生了严重错误，比如断网等,SDK内部会停止推流

**注意：所有回调直接运行在产生事件的各工作线程中，不要在该回调中做任何耗时的操作，或者直接调用推流API。**
````java
 public StreamStatusEventHandler.OnStatusErrorListener mOnStatusErrorListener = new StreamStatusEventHandler.OnStatusErrorListener() {
        @Override
        public void onError(int what, int arg1, int arg2, String msg) {
            //what is the status flag, msg may be null  
            switch (what) {
              // ...
            }
        }
}
public StreamStatusEventHandler.OnStatusInfoListener mOnStatusInfoListener = new StreamStatusEventHandler.OnStatusInfoListener() {
        @Override
        public void onInfo(int what, int arg1, int arg2, String msg) {
//what is the status flag, msg may be null  
            switch (what) {
              // ...
            }
        }
}
````
- 创建KSYStreamer对象
````java
mStreamer = new KSYStreamer(this);
mStreamer.setConfig(config);
mStreamer.setDisplayPreview(mCameraPreview);
mStreamer.setOnStatusListener(mOnStatusListener);
````
- 开始推流  
**注意：初次开启预览后需要在mOnStatusListener回调中收到RecorderConstants.KSYVIDEO_INIT_DONE
事件后调用方才有效。**
````java
mStreamer.startStream();
````
- 推流过程中可动态设置的常用方法
````java
// 切换前后摄像头
mStreamer.switchCamera();
// 开关闪光灯
mStreamer.toggleTorch(true);
// 设置美颜滤镜，关于美颜滤镜的具体定义值及说明请参见后续章节
mStreamer.setBeautyFilter(RecorderConstants.FILTER_BEAUTY_DENOISE);
````
- 停止推流
````java
mStreamer.stopStream();
````
- Activity的生命周期回调处理  
**采集的状态依赖于Activity的生命周期，所以必须在Activity的生命周期中也调用SDK相应的接口。**
```java
public class CameraActivity extends Activity {

    // ...

    @Override
    public void onResume() {
        super.onResume();
        mStreamer.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mStreamer.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStreamer.onDestroy();
    }
}
```

## 六. 功能详细使用说明
* [推流初始化参数](https://github.com/ksvc/KSYStreamer_Android/wiki/%E6%8E%A8%E6%B5%81%E5%88%9D%E5%A7%8B%E5%8C%96%E5%8F%82%E6%95%B0)
* [状态和错误回调](https://github.com/ksvc/KSYStreamer_Android/wiki/%E7%8A%B6%E6%80%81%E5%92%8C%E9%94%99%E8%AF%AF%E5%9B%9E%E8%B0%83)
* [内置美颜](https://github.com/ksvc/KSYStreamer_Android/wiki/%E5%86%85%E7%BD%AE%E7%BE%8E%E9%A2%9C)
* [自定义滤镜](https://github.com/ksvc/KSYStreamer_Android/wiki/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%BB%A4%E9%95%9C)
* [混音](https://github.com/ksvc/KSYStreamer_Android/wiki/%E6%B7%B7%E9%9F%B3)
* [美声](https://github.com/ksvc/KSYStreamer_Android/wiki/%E7%BE%8E%E5%A3%B0)
* [水印](https://github.com/ksvc/KSYStreamer_Android/wiki/%E6%B0%B4%E5%8D%B0)
* [手动对焦](https://github.com/ksvc/KSYStreamer_Android/wiki/%E6%89%8B%E5%8A%A8%E5%AF%B9%E7%84%A6)

## 七. [API接口速查](https://github.com/ksvc/KSYStreamer_Android/wiki/API%E6%8E%A5%E5%8F%A3%E9%80%9F%E6%9F%A5)  
## 八. [接口变更](https://github.com/ksvc/KSYStreamer_Android/wiki/%E6%8E%A5%E5%8F%A3%E5%8F%98%E6%9B%B4)
## 九.[常见问题](https://github.com/ksvc/KSYStreamer_Android/wiki/%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98)
## 十.反馈与建议
- 主页：[金山云](http://www.ksyun.com/)
- 邮箱：<zengfanping@kingsoft.com>
- QQ讨论群：574179720
- Issues: <https://github.com/ksvc/KSYStreamer_Android/issues>
