package com.ksyun.media.streamer.demo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ksyun.media.streamer.encoder.VideoEncodeFormat;
import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.util.device.DeviceInfo;
import com.ksyun.media.streamer.util.device.DeviceInfoTools;

public class DemoActivity extends Activity
        implements OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = DemoActivity.class.getSimpleName();
    private Button mConnectButton;
    private EditText mUrlEditText;
    private EditText mFrameRateEditText;
    private EditText mVideoBitRateEditText;
    private EditText mAudioBitRateEditText;

    private RadioButton mCap360Button;
    private RadioButton mCap480Button;
    private RadioButton mCap720Button;
    private RadioButton mCap1080Button;

    private RadioButton mPreview360Button;
    private RadioButton mPreview480Button;
    private RadioButton mPreview720Button;
    private RadioButton mPreview1080Button;

    private RadioButton mRes360Button;
    private RadioButton mRes480Button;
    private RadioButton mRes540Button;
    private RadioButton mRes720Button;

    private RadioButton mLandscapeButton;
    private RadioButton mPortraitButton;

    private RadioGroup mEncodeGroup;
    private RadioButton mSWButton;
    private RadioButton mHWButton;
    private RadioButton mSW1Button;

    private RadioGroup mEncodeTypeGroup;
    private RadioButton mEncodeWithH264;
    private RadioButton mEncodeWithH265;

    private RadioGroup mSceneGroup;
    private RadioButton mSceneDefaultButton;
    private RadioButton mSceneShowSelfButton;
    private RadioButton mSceneGameButton;
    private RadioGroup mProfileGroup;
    private RadioButton mProfileLowPowerButton;
    private RadioButton mProfileBalanceButton;
    private RadioButton mProfileHighPerfButton;

    private CheckBox mStereoStreamCheckBox;
    private CheckBox mAutoStartCheckBox;
    private CheckBox mShowDebugInfoCheckBox;

    private DeviceInfo mDeviceInfo;
    private boolean mShowDeviceToast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        mConnectButton = (Button) findViewById(R.id.connectBT);
        mConnectButton.setOnClickListener(this);

        mUrlEditText = (EditText) findViewById(R.id.rtmpUrl);
        mFrameRateEditText = (EditText) findViewById(R.id.frameRatePicker);
        mFrameRateEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mVideoBitRateEditText = (EditText) findViewById(R.id.videoBitratePicker);
        mVideoBitRateEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mAudioBitRateEditText = (EditText) findViewById(R.id.audioBitratePicker);
        mAudioBitRateEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mCap360Button = (RadioButton) findViewById(R.id.cap_360);
        mCap480Button = (RadioButton) findViewById(R.id.cap_480);
        mCap720Button = (RadioButton) findViewById(R.id.cap_720);
        mCap1080Button = (RadioButton) findViewById(R.id.cap_1080);
        mPreview360Button = (RadioButton) findViewById(R.id.preview_360);
        mPreview480Button = (RadioButton) findViewById(R.id.preview_480);
        mPreview720Button = (RadioButton) findViewById(R.id.preview_720);
        mPreview1080Button = (RadioButton) findViewById(R.id.preview_1080);
        mRes360Button = (RadioButton) findViewById(R.id.target_360);
        mRes480Button = (RadioButton) findViewById(R.id.target_480);
        mRes540Button = (RadioButton) findViewById(R.id.target_540);
        mRes720Button = (RadioButton) findViewById(R.id.target_720);
        mLandscapeButton = (RadioButton) findViewById(R.id.orientationbutton1);
        mPortraitButton = (RadioButton) findViewById(R.id.orientationbutton2);
        mEncodeGroup = (RadioGroup) findViewById(R.id.encode_group);
        mSWButton = (RadioButton) findViewById(R.id.encode_sw);
        mHWButton = (RadioButton) findViewById(R.id.encode_hw);
        mSW1Button = (RadioButton) findViewById(R.id.encode_sw1);
        mEncodeTypeGroup = (RadioGroup) findViewById(R.id.encode_type);
        mEncodeWithH264 = (RadioButton) findViewById(R.id.encode_h264);
        mEncodeWithH265 = (RadioButton) findViewById(R.id.encode_h265);
        mSceneGroup = (RadioGroup) findViewById(R.id.encode_scene);
        mSceneDefaultButton = (RadioButton) findViewById(R.id.encode_scene_default);
        mSceneShowSelfButton = (RadioButton) findViewById(R.id.encode_scene_show_self);
        mSceneGameButton = (RadioButton) findViewById(R.id.encode_scene_game);
        mProfileGroup = (RadioGroup) findViewById(R.id.encode_profile);
        mProfileLowPowerButton = (RadioButton) findViewById(R.id.encode_profile_low_power);
        mProfileBalanceButton = (RadioButton) findViewById(R.id.encode_profile_balance);
        mProfileHighPerfButton = (RadioButton) findViewById(R.id.encode_profile_high_perf);
        mStereoStreamCheckBox = (CheckBox) findViewById(R.id.stereo_stream);
        mAutoStartCheckBox = (CheckBox) findViewById(R.id.autoStart);
        mShowDebugInfoCheckBox = (CheckBox) findViewById(R.id.print_debug_info);

        updateUI();
        mEncodeTypeGroup.setOnCheckedChangeListener(this);
        mEncodeGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        //init encode info
        //若在硬编白名单中存在设备信息，则参考白名单信息进行配置
        DeviceInfo lastDeviceInfo = mDeviceInfo;
        mDeviceInfo = DeviceInfoTools.getInstance().getDeviceInfo();
        if (mDeviceInfo != null) {
            Log.i(TAG, "deviceInfo:" + mDeviceInfo.printDeviceInfo());
            if (!mShowDeviceToast || (lastDeviceInfo != null && !mDeviceInfo.compareDeviceInfo
                    (lastDeviceInfo))) {
                if (mDeviceInfo.encode_h264 == DeviceInfo.ENCODE_HW_SUPPORT) {
                    //支持硬编，建议使用硬编
                    mHWButton.setChecked(true);
                    Toast.makeText(this, "该设备支持h264硬编，建议您使用硬编", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "该设备可能不在硬编白名单中\n或者不支持硬编\n或者服务器还未返回" +
                            "\n如果支持硬编，欢迎一起更新白名单", Toast.LENGTH_SHORT).show();
                }
                mShowDeviceToast = true;
            }
        }
    }

    private void setEnableRadioGroup(RadioGroup radioGroup, boolean enable) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(enable);
        }
    }

    private void updateUI() {
        if (mHWButton.isChecked() || mEncodeWithH265.isChecked()) {
            setEnableRadioGroup(mSceneGroup, false);
            setEnableRadioGroup(mProfileGroup, false);
        } else {
            setEnableRadioGroup(mSceneGroup, true);
            setEnableRadioGroup(mProfileGroup, true);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        updateUI();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.connectBT:
                int frameRate = 0;
                int videoBitRate = 0;
                int audioBitRate = 0;
                int captureResolution;
                int previewResolution;
                int targetResolution;
                int encodeType;
                int encodeMethod;
                int encodeScene;
                int encodeProfile;
                int orientation;
                boolean stereoStream;
                boolean startAuto;
                boolean showDebugInfo;

                if (!TextUtils.isEmpty(mUrlEditText.getText())
                        && mUrlEditText.getText().toString().startsWith("rtmp")) {
                    if (!TextUtils.isEmpty(mFrameRateEditText.getText().toString())) {
                        frameRate = Integer.parseInt(mFrameRateEditText.getText()
                                .toString());
                    }

                    if (!TextUtils.isEmpty(mVideoBitRateEditText.getText().toString())) {
                        videoBitRate = Integer.parseInt(mVideoBitRateEditText.getText()
                                .toString());
                    }

                    if (!TextUtils.isEmpty(mAudioBitRateEditText.getText().toString())) {
                        audioBitRate = Integer.parseInt(mAudioBitRateEditText.getText()
                                .toString());
                    }

                    if (mCap360Button.isChecked()) {
                        captureResolution = StreamerConstants.VIDEO_RESOLUTION_360P;
                    } else if (mCap480Button.isChecked()) {
                        captureResolution = StreamerConstants.VIDEO_RESOLUTION_480P;
                    } else if (mCap720Button.isChecked()) {
                        captureResolution = StreamerConstants.VIDEO_RESOLUTION_720P;
                    } else {
                        captureResolution = StreamerConstants.VIDEO_RESOLUTION_1080P;
                    }

                    if (mPreview360Button.isChecked()) {
                        previewResolution = StreamerConstants.VIDEO_RESOLUTION_360P;
                    } else if (mPreview480Button.isChecked()) {
                        previewResolution = StreamerConstants.VIDEO_RESOLUTION_480P;
                    } else if (mPreview720Button.isChecked()) {
                        previewResolution = StreamerConstants.VIDEO_RESOLUTION_720P;
                    } else {
                        previewResolution = StreamerConstants.VIDEO_RESOLUTION_1080P;
                    }

                    if (mRes360Button.isChecked()) {
                        targetResolution = StreamerConstants.VIDEO_RESOLUTION_360P;
                    } else if (mRes480Button.isChecked()) {
                        targetResolution = StreamerConstants.VIDEO_RESOLUTION_480P;
                    } else if (mRes540Button.isChecked()) {
                        targetResolution = StreamerConstants.VIDEO_RESOLUTION_540P;
                    } else {
                        targetResolution = StreamerConstants.VIDEO_RESOLUTION_720P;
                    }

                    if (mEncodeWithH265.isChecked()) {
                        encodeType = AVConst.CODEC_ID_HEVC;
                    } else {
                        encodeType = AVConst.CODEC_ID_AVC;
                    }

                    if (mHWButton.isChecked()) {
                        encodeMethod = StreamerConstants.ENCODE_METHOD_HARDWARE;
                        mSceneGroup.setClickable(false);
                    } else if (mSWButton.isChecked()) {
                        mSceneGroup.setClickable(true);
                        encodeMethod = StreamerConstants.ENCODE_METHOD_SOFTWARE;
                    } else {
                        mSceneGroup.setClickable(true);
                        encodeMethod = StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT;
                    }

                    if (mSceneShowSelfButton.isChecked()) {
                        encodeScene = VideoEncodeFormat.ENCODE_SCENE_SHOWSELF;
                    } else if (mSceneGameButton.isChecked()) {
                        encodeScene = VideoEncodeFormat.ENCODE_SCENE_GAME;
                    } else {
                        encodeScene = VideoEncodeFormat.ENCODE_SCENE_DEFAULT;
                    }

                    if (mProfileLowPowerButton.isChecked()) {
                        encodeProfile = VideoEncodeFormat.ENCODE_PROFILE_LOW_POWER;
                    } else if (mProfileBalanceButton.isChecked()) {
                        encodeProfile = VideoEncodeFormat.ENCODE_PROFILE_BALANCE;
                    } else {
                        encodeProfile = VideoEncodeFormat.ENCODE_PROFILE_HIGH_PERFORMANCE;
                    }

                    if (mLandscapeButton.isChecked()) {
                        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    } else if (mPortraitButton.isChecked()) {
                        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    } else {
                        orientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
                    }

                    stereoStream = mStereoStreamCheckBox.isChecked();
                    startAuto = mAutoStartCheckBox.isChecked();
                    showDebugInfo = mShowDebugInfoCheckBox.isChecked();

                    CameraActivity.startActivity(getApplicationContext(), 0,
                            mUrlEditText.getText().toString(), frameRate, videoBitRate,
                            audioBitRate, captureResolution, previewResolution, targetResolution,
                            orientation, encodeType, encodeMethod, encodeScene, encodeProfile,
                            stereoStream, startAuto, showDebugInfo);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
