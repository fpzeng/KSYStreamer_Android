package com.ksyun.media.streamer.demo;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.ksyun.media.streamer.capture.CameraCapture;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.util.device.DeviceInfo;
import com.ksyun.media.streamer.util.device.DeviceInfoTools;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Standard demo fragment.
 */

public class BaseDemoFragment extends DemoFragment {
    private static final String TAG = "BaseDemoFragment";

    @BindView(R.id.frameRatePicker)
    protected EditText mFrameRateEditText;
    @BindView(R.id.videoBitratePicker)
    protected EditText mVideoBitRateEditText;
    @BindView(R.id.audioBitratePicker)
    protected EditText mAudioBitRateEditText;

    @BindView(R.id.camera_face_group)
    protected RadioGroup mFacingGroup;
    @BindView(R.id.target_res_group)
    protected RadioGroup mTargetResGroup;
    @BindView(R.id.orientation_group)
    protected RadioGroup mOrientationGroup;
    @BindView(R.id.encode_method)
    protected RadioGroup mEncodeMethodGroup;

    @BindView(R.id.autoStart)
    protected CheckBox mAutoStartCheckBox;
    @BindView(R.id.print_debug_info)
    protected CheckBox mShowDebugInfoCheckBox;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.base_demo_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    protected void loadParams(BaseCameraActivity.BaseStreamConfig config, String url) {
        // initial value
        config.mFrameRate = 15.0f;
        config.mVideoKBitrate = 800;
        config.mAudioKBitrate = 48;
        config.mUrl = url;

        // video frame rate
        if (!TextUtils.isEmpty(mFrameRateEditText.getText().toString())) {
            config.mFrameRate = Integer.parseInt(mFrameRateEditText.getText().toString());
        }

        // video bitrate
        if (!TextUtils.isEmpty(mVideoBitRateEditText.getText().toString())) {
            config.mVideoKBitrate = Integer.parseInt(mVideoBitRateEditText.getText().toString());
        }

        // audio bitrate
        if (!TextUtils.isEmpty(mAudioBitRateEditText.getText().toString())) {
            config.mAudioKBitrate = Integer.parseInt(mAudioBitRateEditText.getText().toString());
        }

        if (mFacingGroup.getCheckedRadioButtonId() == R.id.camera_face_front) {
            config.mCameraFacing = CameraCapture.FACING_FRONT;
        } else {
            config.mCameraFacing = CameraCapture.FACING_BACK;
        }

        // video resolution
        switch (mTargetResGroup.getCheckedRadioButtonId()) {
            case R.id.target_360:
                config.mTargetResolution = StreamerConstants.VIDEO_RESOLUTION_360P;
                break;
            case R.id.target_480:
                config.mTargetResolution = StreamerConstants.VIDEO_RESOLUTION_480P;
                break;
            case R.id.target_540:
                config.mTargetResolution = StreamerConstants.VIDEO_RESOLUTION_540P;
                break;
            default:
                config.mTargetResolution = StreamerConstants.VIDEO_RESOLUTION_720P;
        }

        // orientation
        switch (mOrientationGroup.getCheckedRadioButtonId()) {
            case R.id.orientation_portrait:
                config.mOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case R.id.orientation_landscape:
                config.mOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            default:
                config.mOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
        }

        // encode method
        switch (mEncodeMethodGroup.getCheckedRadioButtonId()) {
            case R.id.encode_auto:
                if (isHw264EncoderSupported()) {
                    config.mEncodeMethod = StreamerConstants.ENCODE_METHOD_HARDWARE;
                } else {
                    config.mEncodeMethod = StreamerConstants.ENCODE_METHOD_SOFTWARE;
                }
                break;
            case R.id.encode_hw:
                config.mEncodeMethod = StreamerConstants.ENCODE_METHOD_HARDWARE;
                break;
            case R.id.encode_sw:
                config.mEncodeMethod = StreamerConstants.ENCODE_METHOD_SOFTWARE;
                break;
            default:
                config.mEncodeMethod = StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT;
        }

        config.mAutoStart = mAutoStartCheckBox.isChecked();
        config.mShowDebugInfo = mShowDebugInfoCheckBox.isChecked();
    }

    // check HW encode white list
    protected boolean isHw264EncoderSupported() {
        DeviceInfo deviceInfo = DeviceInfoTools.getInstance().getDeviceInfo();
        if (deviceInfo != null) {
            Log.i(TAG, "deviceInfo:" + deviceInfo.printDeviceInfo());
            return deviceInfo.encode_h264 == DeviceInfo.ENCODE_HW_SUPPORT;
        }
        return false;
    }

    @Override
    public void start(String url) {
        BaseCameraActivity.BaseStreamConfig config = new BaseCameraActivity.BaseStreamConfig();
        loadParams(config, url);
        BaseCameraActivity.startActivity(getActivity(), config, BaseCameraActivity.class);
    }
}
