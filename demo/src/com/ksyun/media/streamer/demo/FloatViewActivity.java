package com.ksyun.media.streamer.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ksyun.media.streamer.kit.KSYStreamer;


/**
 * Activity to show float camera view streaming.
 */

public class FloatViewActivity extends Activity {
    private static final String TAG = "FloatViewActivity";

    private final static int OVERLAY_PERMISSION_RESULT_CODE = 10;

    private GLSurfaceView mCameraView;
    private int mLastRotation;
    private OrientationEventListener mOrientationEventListener;

    //preview window just demo
    private FloatView mFloatLayout; //悬浮窗口的layout, 可以是xml,也可以在代码中创建
    private ImageView mFloatClose;  //关闭悬浮窗口
    private ImageView mFloatBack;   //跳转到上一个页面，不停止推流
    private WindowManager.LayoutParams mWmParams;  //layout的布局
    private WindowManager mWindowManager;
    private volatile boolean mPreviewWindowShow = false;  //悬浮摄像头预览window是否正在显示

    //ButtonListener
    private ButtonObserver mObserverButton;  //所有按键响应

    // logo path
    private String mLogoPath = "file:///sdcard/test.png";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.goods_activity);
        mObserverButton = new ButtonObserver();
        initSurfaceWindow();
        addSurfaceWindow();
        mLastRotation = getDisplayRotation();

        mOrientationEventListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation = getDisplayRotation();
                if (rotation != mLastRotation) {
                    Log.d(TAG, "Rotation changed " + mLastRotation + "->" + rotation);
                    onSwitchRotate();
                    mLastRotation = rotation;
                }
            }
        };
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        //KSYGlobalStreamer.getInstance().onResume();
        //6.0 需要检查overlay权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_RESULT_CODE);
            }
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        //KSYGlobalStreamer.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
        removeSurfaceWindow();
        KSYGlobalStreamer.setInstance(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackoffClick();
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onBackoffClick() {
        KSYGlobalStreamer.getInstance().onPause();
        FloatViewActivity.this.finish();
    }

    /**
     * 初始化悬浮窗口示例
     */
    private void initSurfaceWindow() {
        if (mWindowManager == null) {
            mWmParams = new WindowManager.LayoutParams();
            mWindowManager = (WindowManager) getApplication().
                    getSystemService(Application.WINDOW_SERVICE);

            //设置window type
            mWmParams.type = WindowManager.LayoutParams.TYPE_TOAST;

            //设置图片格式，效果为背景透明
            mWmParams.format = PixelFormat.RGBA_8888;

            //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
            mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //开启硬件加速，以支持TextureView
            mWmParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            //接收touch事件
            mWmParams.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            //排版不受限制
            mWmParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            //调整悬浮窗显示的停靠位置为右侧底部
            mWmParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;

            // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
            mWmParams.x = 0;
            mWmParams.y = 0;

            //设置悬浮窗口长宽数据(这里取屏幕长宽的等比率缩小值作为悬浮窗口的长宽)
            int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
            int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
            int width;
            int height;

            boolean isLandscape = (getDisplayRotation() % 180) != 0;
            if ((isLandscape && screenWidth < screenHeight) ||
                    (!isLandscape) && screenWidth > screenHeight) {
                screenWidth = mWindowManager.getDefaultDisplay().getHeight();
                screenHeight = mWindowManager.getDefaultDisplay().getWidth();
            }

            if (screenWidth < screenHeight) {
                width = align(screenWidth / 3, 8);
                height = align(screenHeight / 3, 8);
            } else {
                width = align(screenWidth / 3, 8);
                height = align(screenHeight / 3, 8);
            }
            mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            LayoutInflater inflater = LayoutInflater.from(getApplication());

            //获取浮动窗口视图所在布局
            mFloatLayout = (FloatView) inflater.inflate(R.layout.float_preview, null);

            mFloatClose = new ImageView(this);
            mFloatClose.setImageResource(R.drawable.float_close);
            mFloatClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackoffClick();
                }
            });

            mFloatBack = (ImageView) findViewById(R.id.float_back);
            mFloatBack.setOnClickListener(mObserverButton);

            mCameraView = new GLSurfaceView(this);
            KSYGlobalStreamer.getInstance().setDisplayPreview(mCameraView);
            RelativeLayout.LayoutParams previewLayoutParams =
                    new RelativeLayout.LayoutParams(width, height);
            mFloatLayout.addView(mCameraView, previewLayoutParams);
            RelativeLayout.LayoutParams closeParams =
                    new RelativeLayout.LayoutParams(50, 50);
            closeParams.setMargins(10,10,0,0);
            mFloatLayout.addView(mFloatClose, closeParams);

            mFloatLayout.setWmParams(mWmParams);
        }
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.float_back:
                    onBackoffClick();
                    break;
                default:
                    break;
            }
        }
    }

    private void addSurfaceWindow() {
        if (mWindowManager != null) {
            //添加mFloatLayout
            mWindowManager.addView(mFloatLayout, mWmParams);
        }
        mPreviewWindowShow = true;
    }

    private void removeSurfaceWindow() {
        if (mWindowManager != null) {
            mWindowManager.removeViewImmediate(mFloatLayout);
        }
        mPreviewWindowShow = false;
    }

    private void updateViewPosition() {
        if (mWmParams != null && mWindowManager != null) {
            mWmParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;

            // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
            mWmParams.x = 0;
            mWmParams.y = 0;
            mWindowManager.updateViewLayout(mFloatLayout, mWmParams);  //刷新显示
        }
    }

    private int align(int val, int align) {
        return (val + align - 1) / align * align;
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

    /**
     * 申请overlay权限窗口返回
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_RESULT_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    Toast.makeText(FloatViewActivity.this, "SYSTEM_ALERT_WINDOW not granted",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 旋转摄像头预览示例代码
     */
    private void onSwitchRotate() {
        if (mPreviewWindowShow) {
            int rotation = getDisplayRotation();
            Log.i(TAG, "onSwitchRotate: " + mLastRotation + "->" + rotation);
            KSYGlobalStreamer.getInstance().setRotateDegrees(rotation);

            boolean isLastLandscape = (mLastRotation % 180) != 0;
            boolean isLandscape = (rotation % 180) != 0;
            if (isLastLandscape != isLandscape) {
                int width = mCameraView.getHeight();
                int height = mCameraView.getWidth();
                RelativeLayout.LayoutParams previewLayoutParams =
                        (RelativeLayout.LayoutParams) mCameraView.getLayoutParams();
                previewLayoutParams.width = width;
                previewLayoutParams.height = height;
                //更新CameraPreview布局
                mFloatLayout.updateViewLayout(mCameraView, previewLayoutParams);

                updateViewPosition();
                updateWaterMark(isLandscape);
            }
        }
    }

    // update water mark position while screen orientation changed
    private void updateWaterMark(boolean isLandscape) {
        KSYStreamer streamer = KSYGlobalStreamer.getInstance();
        streamer.hideWaterMarkLogo();
        streamer.hideWaterMarkTime();
        if (!isLandscape) {
            streamer.showWaterMarkLogo(mLogoPath, 0.08f, 0.04f, 0.20f, 0, 0.8f);
            streamer.showWaterMarkTime(0.03f, 0.01f, 0.35f, Color.WHITE, 1.0f);
        } else {
            streamer.showWaterMarkLogo(mLogoPath, 0.05f, 0.09f, 0, 0.20f, 0.8f);
            streamer.showWaterMarkTime(0.01f, 0.03f, 0.22f, Color.WHITE, 1.0f);
        }
    }
}
