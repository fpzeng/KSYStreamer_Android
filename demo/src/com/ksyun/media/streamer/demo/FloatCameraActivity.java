package com.ksyun.media.streamer.demo;

import android.content.Intent;
import android.util.Log;
import android.view.Surface;

import butterknife.OnClick;

/**
 * Float view streaming demo activity.
 */

public class FloatCameraActivity extends BaseCameraActivity {
    public static final String TAG = "FloatCameraActivity";

    protected boolean mSwitchToFloatView;

    @Override
    protected int getLayoutId() {
        return R.layout.float_camera_activity;
    }

    @OnClick(R.id.view_add)
    protected void onViewAddClick() {
        mSwitchToFloatView = true;
        Intent intent = new Intent(getApplicationContext(), FloatViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        KSYGlobalStreamer.setInstance(mStreamer);
        startActivity(intent);
    }

    @Override
    protected void handleOnResume() {
        // 重新设置预览View，防止从悬浮窗回来后黑屏
        mStreamer.setDisplayPreview(mGLSurfaceView);
        // 重新设置旋转参数，防止从悬浮窗回来后画面变形
        mStreamer.setRotateDegrees(getDisplayRotation());
        // 重新绘制水印，防止从悬浮窗出来后水印大小出错
        hideWaterMark();
        showWaterMark();
        // 调用KSYStreamer的onResume接口
        mStreamer.onResume();
        // 停止背景图采集
        mStreamer.stopImageCapture();
        // 开启摄像头采集
        startCameraPreviewWithPermCheck();
        // 如果onPause中切到了DummyAudio模块，可以在此恢复
        mStreamer.setUseDummyAudioCapture(false);
    }

    @Override
    protected void handleOnPause() {
        // 调用KSYStreamer的onPause接口
        mStreamer.onPause();
        if (!mSwitchToFloatView) {
            // 停止摄像头采集，然后开启背景图采集，以实现后台背景图推流功能
            mStreamer.stopCameraPreview();
            mStreamer.startImageCapture(mBgImagePath);
            // 如果希望App切后台后，停止录制主播端的声音，可以在此切换为DummyAudio采集，
            // 该模块会代替mic采集模块产生静音数据，同时释放占用的mic资源
            mStreamer.setUseDummyAudioCapture(true);
        }
        mSwitchToFloatView = false;
    }

    private int getDisplayRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }
}
