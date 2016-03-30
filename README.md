# KSY Streamer Android SDK使用手册

## 阅读对象
本文档面向所有使用该SDK的开发人员, 测试人员等, 要求读者具有一定的Android编程开发经验.

## KSY Streamer Android SDK 概述
KSY Streamer Android SDK是金山云推出的 Android 平台上使用的软件开发工具包(SDK), 负责采集和推流。

## 主要功能点

* 自适应网络，软编和硬编(new)都可根据实际网络情况动态调整目标码率，保证流畅性
* 音频编码：AAC
* 视频编码：H.264 
* 推流协议：RTMP
* 视频分辨率：支持360P,480P,540P和720P
* 屏幕朝向： 可支持固定横屏或固定竖屏推流
* iOS摄像头：前, 后置摄像头（可动态切换）
* 音视频目标码率：可设
* 闪光灯：开/关
* 内置美颜选择功能
* 支持软编和硬编
* 美颜接口 (new)
* 混音功能 (new)
* 前置镜像功能 (new)


##使用方法
### 配置项目
使用金山云Android直播推流SDK需引入相应的资源，并在项目中添加依赖关系：
- libs/armeabi-v7a/libDenoise_export.so
- libs/armeabi-v7a/libksystreamer.so
- libs/armeabi-v7a/libksyyuv.so
- libs/armeabi-v7a/liblame.so  (new)
- libs/armeabi-v7a/libreverb.so (new)
- libs/ksylive3.0.jar

其中jar包的包名是：
- com.ksy.recordlib.service.core

###系统权限
使用本SDK时需要在AndroidManifest.xml里申请相应权限
```
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
    
```
##代码示例
. 布局文件
```
<android.opengl.GLSurfaceView
	android:id="@+id/camera_preview"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	android:layout_alignParentTop="true" 
	android:layout_alignParentBottom="true"/>
```
. 初始化GLSurfaceView
```
GLSurfaceView mCameraPreview = (GLSurfaceView)findViewById(R.id.camera_preview)
```
. 实例化并初始化KSYStreamerConfig
KSYStreamerConfig采用了Builder模式，需先创建对应的Builder对象。Builder是类KSYStreamerConfig的内部静态公开类。
```
KSYStreamerConfig.Builder builder = new KSYStreamerConfig.Builder();
```
- Builder类中可配置的参数有：

|        方法    	 |       功能      |
|:------------------:|:---------------:|
|setSampleAudioRateInHz|设置音频采样率|
|setFrameRate|设置推流编码帧率|
|setInitVideoBitrate|设置初始视频编码平均码率|
|setMaxVideoBitrate|设置最大视频编码平均码率(目标平均码率)|
|setMinVideoBitrate|设置最小视频编码平均码率|
|setAudioBitrate|设置音频编码码率|
|setVideoResolution|设置分辨率等级|
|setDefaultLandscape|是否以横屏推流，必须同时在manifest或代码里设置Activity为landscape|
|setmUrl|设置推流地址|
|setAppId|设置AppId，用于SDK鉴权|
|setAccessKey|设置AccessKey，用于SDK鉴权|
|setSecretKeySign|设置SecretKeySign，用于SDK鉴权|
|setTimeSecond|设置时间戳，用于SDK鉴权|
|setAutoAdjustBitrate|是否打开自适应码率功能，默认打开|
|setStartPreviewManual|设置手动启动预览,除非调用startCameraPreview接口否则不自动预览，默认关闭|
|setEnableCameraMirror|设置开启前置摄像头镜像，默认关闭|
|setBeautyFilter|设置内置美颜类别(目前软编只支持一种)|

其中分辨率等级可以设置为RecorderConstants.VIDEO_RESOLUTION_360P,RecorderConstants.VIDEO_RESOLUTION_480P,RecorderConstants.VIDEO_RESOLUTION_540P或RecorderConstants.VIDEO_RESOLUTION_720P。内置美颜种类可以设置为FILTER_BEAUTY_DISABLE(不使用美颜)、FILTER_BEAUTY_DENOISE、FILTER_BEAUTY、FILTER_SKINWHITEN、FILTER_BEAUTY_PLUS或FILTER_BEAUTY_PLUS，其中软编只可以设置为FILTER_BEAUTY_DISABLE(不使用美颜)和FILTER_BEAUTY_DENOISE。

. 创建监听器
在类KSYStreamer中定义了接口onStatusListener，开发者实现并设置给SDK之后，可通过onStatus回调收到相应的信息，其中SDK预定义的状态码如下所示。
- SDK预定义的常量   


|        名称    	 |       数值      |       含义      |
|:------------------:|:----------:|:-------------------:|
|KSYVIDEO_OPEN_STREAM_SUCC|0|推流成功|
|KSYVIDEO_INIT_DONE|1000|首次开启预览完成初始化的通知,表示可以进行推流，默认整个KSYStreamer生命周期只会回调一次|
|KSYVIDEO_AUTH_FAILED|-1001|鉴权失败|
|KSYVIDEO_ENCODED_FRAMES_THRESHOLD|-1002|鉴权失败后编码帧数达上限|
|KSYVIDEO_ENCODED_FRAMES_FAILED|-1003|编码失败|
|KSYVIDEO_CODEC_OPEN_FAILED|-1004|推流失败|
|KSYVIDEO_CODEC_GUESS_FORMAT_FAILED|-1005|推流失败|
|KSYVIDEO_OPEN_FILE_FAILED|-1006|推流失败|
|KSYVIDEO_WRITE_FRAME_FAILED|-1007|推流过程中断网|
|KSYVIDEO_OPEN_CAMERA_FAIL|-2001|打开摄像头失败|
|KSYVIDEO_CAMERA_DISABLED|-2002|打开摄像头失败|
|KSYVIDEO_NETWORK_NOT_GOOD|-3001|网络状况不佳|
|KSYVIDEO_EST_BW_RAISE|-3002|码率开始上调的通知|
|KSYVIDEO_EST_BW_DROP|-3003|码率开始下调的通知|

在使用SDK开始推流之后，SDK会发起认证请求，如果鉴权失败会通过回调告知开发者出现**KSYVIDEO_AUTH_FAILED**，反之则没有。
如果鉴权失败，则编码的帧数是会有上限，当编码帧率为15FPS时，可推流时间大约是在13分钟至26分钟之间。推流编码的帧数达到上限后会通过回调函数告知开发者出现**KSYVIDEO_ENCODED_FRAMES_THRESHOLD**，并且会**停止推流**。认证相关的设置请参照Demo。
- 创建onStatusListener
```
public KSYStreamer.onStatusListener mOnStatusListener = new KSYStreamer.onStatusListener() {
		@Override
		public void onStatus(int what, int arg1, int arg2) 
		{
			switch (what)
			{
				case RecorderConstants.KSYVIDEO_ENCODED_FRAMES_THRESHOLD:
					Log.d("KSYVideoErrror", "KSYVIDEO_ENCODED_FRAME_THRESHOLD");
					break;
				case RecorderConstants.KSYVIDEO_AUTH_FAILED:
					Log.d("KSYVideoErrror", "KSYVIDEO_AUTH_ERROR");
					break;
				case RecorderConstants.KSYVIDEO_NETWORK_NOT_GOOD:
					mHandler.obtainMessage(what, "network not good").sendToTarget();
					break;
			}
		}
	};
```

. 实例化并创建KSYRecordClient
```
mStreamer = new KSYStreamer(this);
mStreamer.setDisplayPreview(mCameraPreview);
mStremer.setConfig(builder.build());
mStremer.setOnStatusListener(mOnStatusListener);
```
. 开始推流
目前固定竖屏推流。如果需要横屏推流，可以联系我们。
```
mStreamer.start();
```
. 切换前后摄像头
```
mStreamer.switchCamera();
```
. 设置闪关灯
```
boolean flashSwitch = true; // true为打开闪光灯，false为关闭闪关灯
mStreamer.toggleTorch(flashSwitch)
```

.  获取已上传数据量
```
// 单位：KB
mUploadedDataSize = mStreamer.getUploadedKBytes()
```

. 停止推流
```
mStreamer.stop();
```

. 初始化完成的回调

首次开启预览完成初始化的通知,表示可以进行推流。通过OnStatusListener()发送，状态码为KSYVIDEO_INIT_DONE（1000）。
默认整个KSYStreamer生命周期只会回调一次。如希望在摄像头reopen的场景继续得到回调（比如用户按Home键，KSYStreamer会关掉并释放摄像头，再次返回重新初始化摄像头）需要设置setInitDoneCallbackEnable(true)，这个调用仅对**下一次**初始化有效。
```
 mStreamer.setInitDoneCallbackEnable(true);
```

. 注意事项
采集的状态依赖于Activity的生命周期，所以必须在Activity的生命周期中也调用SDK相应的接口，例如：onPause, onResume。
预览区域默认全屏，暂不支持自定义分辨率。
如有其它需求可以联系[我们](http://www.ksyun.com/)
##反馈与建议
- 主页：[金山云](http://www.ksyun.com/)
- 邮箱：<linsong2@kingsoft.com>
