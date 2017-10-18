package com.ksyun.media.streamer.demo;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.streamer.capture.camera.CameraTouchHelper;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.util.gles.GLRender;
import com.lht.paintview.PaintView;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Standard demo with almost full function.
 */

public class StdCameraActivity extends BaseCameraActivity {
    public static final String TAG = "StdCameraActivity";
    protected static final String START_RECORD = "开始录制";
    protected static final String STOP_RECORD = "停止录制";

    // 预览View类型定义
    public static final int PREVIEW_TYPE_GLSURFACEVIEW = 1;
    public static final int PREVIEW_TYPE_TEXTUREVIEW = 2;
    public static final int PREVIEW_TYPE_OFFSCREEN = 3;

    // 前后台切换处理模式定义
    /**
     * 后台正常推流模式
     */
    public static final int BG_SWITCH_MODE_NORMAL_STREAMING = 1;
    /**
     * 后台保留最后一帧模式
     */
    public static final int BG_SWITCH_MODE_KEEP_LAST_FRAME = 2;
    /**
     * 后台背景图推流模式
     */
    public static final int BG_SWITCH_MODE_BITMAP_STREAMING = 3;

    @BindView(R.id.texture_view)
    protected TextureView mTextureView;
    @BindView(R.id.camera_hint)
    protected CameraHintView mCameraHintView;
    @BindView(R.id.view_paint)
    protected PaintView mPaintView;
    @BindView(R.id.camera_expose_bar)
    protected View mCameraExposeBar;
    @BindView(R.id.camera_expose_sb)
    protected SeekBar mCameraExposeSeekBar;
    @BindView(R.id.function_type)
    protected Spinner mFunctionTypeSpinner;
    @BindView(R.id.function_detail)
    protected LinearLayout mFunctionDetailLayout;
    @BindView(R.id.start_record_tv)
    protected TextView mRecordingText;

    protected Fragment mEmptyFragment;
    protected WaterMarkFragment mWaterMarkFragment;
    protected StreamFuncFragment mStreamFuncFragment;
    protected AudioFuncFragment mAudioFuncFragment;
    protected VideoFilterFragment mVideoFilterFragment;

    protected boolean mRecording;
    protected int mLastRotation;
    protected OrientationEventListener mOrientationEventListener;
    protected BluetoothHeadsetUtils mBluetoothHelper;

    protected String mRecordUrl = mSdcardPath + "/rec_test.mp4";

    public static class StdStreamConfig extends BaseStreamConfig {
        public int mCaptureResolution;
        public int mPreviewResolution;
        public int mPreviewViewType;
        public int mBgSwitchMode;
        public int mVideoCodecId;
        public int mVideoEncodeScene;
        public int mVideoEncodeProfile;
        public int mAudioEncodeProfile;
        public boolean mZoomFocus;
        public boolean mStereoStream;
        public boolean mBluetoothMicFirst;
    }

    @Override
    protected BaseStreamConfig getConfig(Bundle bundle) {
        return new StdStreamConfig().fromJson(bundle.getString("config"));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.std_camera_activity;
    }

    @Override
    protected void setDisplayPreview() {
        StdStreamConfig config = (StdStreamConfig) mConfig;
        if (config.mPreviewViewType == PREVIEW_TYPE_GLSURFACEVIEW) {
            // 使用GLSurfaceView预览
            mGLSurfaceView.setVisibility(View.VISIBLE);
            mStreamer.setDisplayPreview(mGLSurfaceView);
        } else if (config.mPreviewViewType == PREVIEW_TYPE_TEXTUREVIEW) {
            // 使用TextureView预览
            mTextureView.setVisibility(View.VISIBLE);
            mStreamer.setDisplayPreview(mTextureView);
        }
        // 使用离屏预览，不需要设置预览View
    }

    protected View getDisplayPreview() {
        StdStreamConfig config = (StdStreamConfig) mConfig;
        switch (config.mPreviewViewType) {
            case PREVIEW_TYPE_GLSURFACEVIEW:
                return mGLSurfaceView;
            case PREVIEW_TYPE_TEXTUREVIEW:
                return mTextureView;
            default:
                return null;
        }
    }

    protected void createFragments() {
        mEmptyFragment = new Fragment();
        mWaterMarkFragment = new WaterMarkFragment();
        mStreamFuncFragment = new StreamFuncFragment();
        mAudioFuncFragment = new AudioFuncFragment();
        mVideoFilterFragment = new VideoFilterFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.function_detail, mEmptyFragment);
        transaction.add(R.id.function_detail, mWaterMarkFragment);
        transaction.add(R.id.function_detail, mStreamFuncFragment);
        transaction.add(R.id.function_detail, mAudioFuncFragment);
        transaction.add(R.id.function_detail, mVideoFilterFragment);
        hideFragments(transaction);
        transaction.commit();
    }

    protected void hideFragments(FragmentTransaction transaction) {
        transaction.hide(mEmptyFragment);
        transaction.hide(mWaterMarkFragment);
        transaction.hide(mStreamFuncFragment);
        transaction.hide(mAudioFuncFragment);
        transaction.hide(mVideoFilterFragment);
    }

    @Override
    protected void initUI() {
        // 曝光补偿SeekBar处理
        mCameraExposeSeekBar.setOnSeekBarChangeListener(mOnExposeBarChangeListener);

        // 功能选择下拉框
        createFragments();
        String[] items = new String[]{"水印设置", "推流设置", "音频设置", "视频滤镜"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFunctionTypeSpinner.setAdapter(adapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mFunctionTypeSpinner.setPopupBackgroundResource(R.color.transparent1);
        }
        mFunctionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = ((TextView) parent.getChildAt(0));
                if (textView != null) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    textView.setTextColor(getResources().getColor(R.color.font_color_35));
                }
                Fragment fragment;
                switch (position) {
                    case 0:
                        fragment = mWaterMarkFragment;
                        break;
                    case 1:
                        fragment = mStreamFuncFragment;
                        break;
                    case 2:
                        fragment = mAudioFuncFragment;
                        break;
                    case 3:
                        fragment = mVideoFilterFragment;
                        break;
                    default:
                        fragment = mEmptyFragment;
                        break;
                }
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                hideFragments(transaction);
                transaction.show(fragment).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
        mFunctionTypeSpinner.setSelection(0);
    }

    @Override
    protected void config() {
        super.config();
        StdStreamConfig config = (StdStreamConfig) mConfig;

        // 配置摄像头采集分辨率
        mStreamer.setCameraCaptureResolution(config.mCaptureResolution);
        // 配置预览分辨率
        mStreamer.setPreviewResolution(config.mPreviewResolution);

        // 配置视频编码器
        mStreamer.setVideoCodecId(config.mVideoCodecId);
        // 配置视频编码的场景模式
        mStreamer.setVideoEncodeScene(config.mVideoEncodeScene);
        // 配置视频编码的性能模式
        mStreamer.setVideoEncodeProfile(config.mVideoEncodeProfile);

        // 配置音频编码的profile
        mStreamer.setAudioEncodeProfile(config.mAudioEncodeProfile);
        // 配置音频编码的声道数
        mStreamer.setAudioChannels(config.mStereoStream ? 2 : 1);

        // 前后台切换处理，后台正常推流跟后台背景图推流均需禁用自动重复最后一帧
        if (config.mBgSwitchMode == BG_SWITCH_MODE_KEEP_LAST_FRAME) {
            mStreamer.setEnableRepeatLastFrame(true);
        } else {
            mStreamer.setEnableRepeatLastFrame(false);
        }

        // 处理屏幕旋转事件
        if (mConfig.mOrientation == ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR) {
            mOrientationEventListener = new StdOrientationEventListener(this,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        // 触摸对焦和手势缩放功能
        if (config.mZoomFocus) {
            CameraTouchHelper cameraTouchHelper = new CameraTouchHelper();
            cameraTouchHelper.setCameraCapture(mStreamer.getCameraCapture());
            if (getDisplayPreview() != null) {
                getDisplayPreview().setOnTouchListener(cameraTouchHelper);
            }
            // set CameraHintView to show focus rect and zoom ratio
            mCameraHintView.setVisibility(View.VISIBLE);
            cameraTouchHelper.setCameraHintView(mCameraHintView);
        }

        // 对蓝牙mic的支持
        if (config.mBluetoothMicFirst) {
            mBluetoothHelper = new BluetoothHeadsetUtils(this);
            mBluetoothHelper.start();
        }
    }

    @Override
    protected void onStreamerInfo(int what, int msg1, int msg2) {
        super.onStreamerInfo(what, msg1, msg2);
        switch (what) {
            case StreamerConstants.KSY_STREAMER_CAMERA_FACING_CHANGED:
                resetCameraExposure();
                resetExposureBar();
                break;
            case StreamerConstants.KSY_STREAMER_OPEN_FILE_SUCCESS:
                Log.d(TAG, "KSY_STREAMER_OPEN_FILE_SUCCESS");
                startChronometer();
                break;
            case StreamerConstants.KSY_STREAMER_FILE_RECORD_STOPPED:
                Log.d(TAG, "KSY_STREAMER_FILE_RECORD_STOPPED");
                mRecordingText.setText(START_RECORD);
                mRecordingText.postInvalidate();
                mRecording = false;
                stopChronometer();
                break;
            default:
                break;
        }
    }

    @Override
    protected void reStreaming(int err) {
        switch (err) {
            case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_CLOSE_FAILED:
            case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_ERROR_UNKNOWN:
            case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_OPEN_FAILED:
            case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_FORMAT_NOT_SUPPORTED:
            case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_WRITE_FAILED:
                // just stop record without retry.
                stopRecord();
                break;
            case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
            case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                // 仅在编码出错时，切换编码方式后重试
                if (mRecording) {
                    stopRecord();
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startRecord();
                        }
                    }, 100);
                }
            default:
                if (mStreaming) {
                    stopStream();
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startStream();
                        }
                    }, 3000);
                }
                break;
        }
    }

    @Override
    protected void handleOnResume() {
        StdStreamConfig config = (StdStreamConfig) mConfig;

        // 处理屏幕旋转事件
        if (mOrientationEventListener != null) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            if (mOrientationEventListener.canDetectOrientation()) {
                mOrientationEventListener.enable();
            }
            int rotation = getDisplayRotation();
            mIsLandscape = (rotation % 180) != 0;
            mStreamer.setRotateDegrees(rotation);
            mLastRotation = rotation;
        }

        // 各种模式下的前后台切换逻辑
        switch (config.mBgSwitchMode) {
            case BG_SWITCH_MODE_NORMAL_STREAMING:
                mStreamer.onResume();
                // camera may be occupied by other app in background
                startCameraPreviewWithPermCheck();
                break;
            case BG_SWITCH_MODE_KEEP_LAST_FRAME:
                mStreamer.onResume();
                // 开启摄像头采集
                startCameraPreviewWithPermCheck();
                // 如果onPause中切到了DummyAudio模块，可以在此恢复
                mStreamer.setUseDummyAudioCapture(false);
                break;
            case BG_SWITCH_MODE_BITMAP_STREAMING:
            default:
                super.handleOnResume();
                break;
        }
    }

    @Override
    protected void handleOnPause() {
        StdStreamConfig config = (StdStreamConfig) mConfig;

        // 处理屏幕旋转事件
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }

        // 各种模式下的前后台切换逻辑
        switch (config.mBgSwitchMode) {
            case BG_SWITCH_MODE_NORMAL_STREAMING:
                // 后台正常推流模式下，后台继续正常采集摄像头画面
                mStreamer.onPause();
                break;
            case BG_SWITCH_MODE_KEEP_LAST_FRAME:
                mStreamer.onPause();
                // 停止摄像头采集，此时如果正在推流，会重复发送最后一帧视频
                mStreamer.stopCameraPreview();
                // 如果希望App切后台后，停止录制主播端的声音，可以在此切换为DummyAudio采集，
                // 该模块会代替mic采集模块产生静音数据，同时释放占用的mic资源
                mStreamer.setUseDummyAudioCapture(true);
                break;
            case BG_SWITCH_MODE_BITMAP_STREAMING:
            default:
                super.handleOnPause();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothHelper != null) {
            mBluetoothHelper.stop();
        }
    }

    @OnClick(R.id.exposure)
    protected void onExposureClick() {
        if (mCameraExposeBar.getVisibility() == View.VISIBLE) {
            mCameraExposeBar.setVisibility(View.GONE);
            resetCameraExposure();
        } else {
            mCameraExposeBar.setVisibility(View.VISIBLE);
            resetExposureBar();
        }
    }

    @OnClick(R.id.screen_cap)
    protected void onScreenCaptureShotClick() {
        mStreamer.requestScreenShot(new GLRender.ScreenShotListener() {
            @Override
            public void onBitmapAvailable(Bitmap bitmap) {
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",
                        Locale.getDefault());
                String path = Environment.getExternalStorageDirectory().getPath() +
                        "/screenshot_" + dateFormat.format(date) + ".jpg";
                saveBitmap(bitmap, path);
            }
        });
    }

    @OnClick(R.id.start_record_tv)
    protected void onStartRecordClick() {
        if (mRecording) {
            stopRecord();
        } else {
            startRecord();
        }
    }

    //start recording to a local file
    private void startRecord() {
        if(mRecording) {
            return;
        }
        //录制开始成功后会发送StreamerConstants.KSY_STREAMER_OPEN_FILE_SUCCESS消息
        mStreamer.startRecord(mRecordUrl);
        mRecordingText.setText(STOP_RECORD);
        mRecordingText.postInvalidate();
        mRecording = true;
    }

    protected void stopRecord() {
        //录制结束为异步接口，录制结束后，
        //会发送StreamerConstants.KSY_STREAMER_FILE_RECORD_STOPPED消息，在这里再处理UI恢复工作
        mStreamer.stopRecord();
    }

    @Override
    protected void startChronometer() {
        if (!mIsChronometerStarted) {
            super.startChronometer();
        }
    }

    @Override
    protected void stopChronometer() {
        if (mStreaming || mRecording) {
            return;
        }
        super.stopChronometer();
    }

    protected void saveBitmap(Bitmap bitmap, final String path) {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(path));
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(StdCameraActivity.this, "保存截图到 " +
                                path, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(StdCameraActivity.this, "保存截图失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (bos != null) try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 重置曝光补偿
    protected void resetCameraExposure() {
        Camera.Parameters parameters = mStreamer.getCameraCapture().getCameraParameters();
        if (parameters != null) {
            parameters.setExposureCompensation(0);
            mStreamer.getCameraCapture().setCameraParameters(parameters);
        }
    }

    // 初始化SeekBar位置
    protected void resetExposureBar() {
        Camera.Parameters parameters = mStreamer.getCameraCapture().getCameraParameters();
        if (parameters != null) {
            int minValue = parameters.getMinExposureCompensation();
            int maxValue = parameters.getMaxExposureCompensation();
            mCameraExposeSeekBar.setMax(maxValue - minValue);
            mCameraExposeSeekBar.setProgress(-minValue);
        }
    }

    protected int getDisplayRotation() {
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

    protected void onRotationChanged(int rotation) {
        if (mWaterMarkFragment.mWatermarkCB.isChecked()) {
            mWaterMarkFragment.hideWatermark();
            mWaterMarkFragment.showWatermark();
        }
    }

    protected SeekBar.OnSeekBarChangeListener mOnExposeBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            Camera.Parameters parameters = mStreamer.getCameraCapture().getCameraParameters();
            if (parameters != null) {
                int minValue = parameters.getMinExposureCompensation();
                float step = parameters.getExposureCompensationStep();
                int exposure = progress + minValue;
                parameters.setExposureCompensation(exposure);
                mStreamer.getCameraCapture().setCameraParameters(parameters);
                float ev = exposure * step;
                Log.d(TAG, "set exposure compensation to " + ev + "ev");
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private class StdOrientationEventListener extends OrientationEventListener {
        StdOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            int rotation = getDisplayRotation();
            if (rotation != mLastRotation) {
                Log.d(TAG, "Rotation changed " + mLastRotation + "->" + rotation);
                mIsLandscape = (rotation % 180) != 0;
                mStreamer.setRotateDegrees(rotation);
                onRotationChanged(rotation);
                mLastRotation = rotation;
            }
        }
    }
}
