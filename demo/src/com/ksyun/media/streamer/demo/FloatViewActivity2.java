package com.ksyun.media.streamer.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static android.content.ContentValues.TAG;


/**
 * Created by qyvideo on 3/13/17.
 */

public class FloatViewActivity2 extends Activity {

    private GLSurfaceView mSurfaceView;

    //user params
    private boolean mIsLandscape;

    private int mLastRotation;
    private OrientationEventListener mOrientationEventListener;
    private int mPresetOrientation;

    //preview window just demo
    private FloatView mFloatLayout;   //悬浮窗口的layout, 可以是xml,也可以在代码中创建
    private ImageView mFloatClose;   //关闭悬浮窗口
    private ImageView mFloatBack; //跳转到上一个页面，不停止推流
    private WindowManager.LayoutParams mWmParams;  //layout的布局
    private WindowManager mWindowManager;
    private volatile boolean mPreviewWindowShow = false;  //悬浮摄像头预览window是否正在显示

    private final static int PERMISSION_REQUEST_RECORD_AUDIO = 2;  //推流录音权限
    private final static int PERMISSION_REQUEST_CAMERA = 1;  //悬浮窗口摄像头权限

    private final static int OVERLAY_PERMISSION_RESULT_CODE = 10;


    //ButtonListener
    private FloatViewActivity2.ButtonObserver mObserverButton;  //所有按键响应

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
                    mIsLandscape = (rotation % 180) != 0;
                    if (mPresetOrientation == ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR) {
//                        KSYGlobalStreamer.getInstance().setIsLandspace(mIsLandscape);
                    }

                    onSwitchRotate();
                    mLastRotation = rotation;
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        KSYGlobalStreamer.getInstance().startCameraPreview();
        KSYGlobalStreamer.getInstance().onResume();
        boolean canDrawOverlay = true;
        //6.0 需要检查overlay权限
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canDrawOverlay = Settings.canDrawOverlays(this);
        }
        if (!canDrawOverlay) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_RESULT_CODE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        KSYGlobalStreamer.getInstance().onPause();
        KSYGlobalStreamer.getInstance().stopCameraPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeSurfaceWindow();
    }



    /**
     * 初始化悬浮窗口示例
     */
    private void initSurfaceWindow() {
        if (mWindowManager == null) {
            mWmParams = new WindowManager.LayoutParams();
            mWindowManager = (WindowManager) getApplication().
                    getSystemService(getApplication().WINDOW_SERVICE);


            //设置window type
            mWmParams.type = WindowManager.LayoutParams.TYPE_TOAST;

            //设置图片格式，效果为背景透明
            mWmParams.format = PixelFormat.RGBA_8888;

            //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
            mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //接收touch事件
            mWmParams.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            //排版不受限制
            mWmParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            //调整悬浮窗显示的停靠位置为左侧置顶
            mWmParams.gravity = Gravity.LEFT | Gravity.BOTTOM;


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
                    KSYGlobalStreamer.getInstance().stopStream();
                    FloatViewActivity2.this.finish();
                }
            });

            mFloatBack = (ImageView) findViewById(R.id.float_back);
            mFloatBack.setOnClickListener(mObserverButton);

            mSurfaceView = new GLSurfaceView(this);
            KSYGlobalStreamer.getInstance().setDisplayPreview(mSurfaceView);
            RelativeLayout.LayoutParams previewLayoutParams =
                    new RelativeLayout.LayoutParams(width, height);
            mFloatLayout.addView(mSurfaceView, previewLayoutParams);
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
                    FloatViewActivity2.this.finish();
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
            mWmParams.gravity = Gravity.RIGHT | Gravity.TOP;

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
     * 申请权限示例代码
     */
    private void requestPermission() {
        //audio
        int audioperm = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (audioperm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.e(TAG, "No RECORD_AUDIO permission, please check");
                Toast.makeText(this, "No RECORD_AUDIO permission, please check",
                        Toast.LENGTH_LONG).show();
            } else {
                String[] permissions = {Manifest.permission.RECORD_AUDIO};
                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSION_REQUEST_RECORD_AUDIO);
            }
        }

        //camera
        if (mPreviewWindowShow) {
            int cameraPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Log.e(TAG, "No CAMERA permission, please check");
                    Toast.makeText(this, "No CAMERA permission, please check",
                            Toast.LENGTH_LONG).show();
                } else {
                    String[] permissions = {Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE};
                    ActivityCompat.requestPermissions(this, permissions,
                            PERMISSION_REQUEST_CAMERA);
                }
            }
        }
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
                    Toast.makeText(FloatViewActivity2.this, "SYSTEM_ALERT_WINDOW not granted",
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
            Log.i(TAG, "onSwitchRotate:" + rotation);

            boolean isLastLandscape = (mLastRotation % 180) != 0;
            boolean isLandscape = (rotation % 180) != 0;
            if (isLastLandscape != isLandscape) {
                int width = mSurfaceView.getHeight();
                int height = mSurfaceView.getWidth();
                LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(width, height);
                layoutParams.gravity = Gravity.BOTTOM | Gravity.TOP
                        | Gravity.LEFT | Gravity.RIGHT;
                //更新CameraPreview布局
                mFloatLayout.updateViewLayout(mSurfaceView, layoutParams);

                updateViewPosition();
            }
        }
    }


}
