package com.ksyun.media.streamer.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.ksyun.media.streamer.framework.AVConst;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Audio streaming demo fragment.
 */

public class AudioDemoFragment extends DemoFragment {
    private static final String TAG = "BaseDemoFragment";

    @BindView(R.id.audioBitratePicker)
    protected EditText mAudioBitRateEditText;
    @BindView(R.id.aac_profile)
    protected RadioGroup mAACProfileGroup;

    @BindView(R.id.stereo_stream)
    protected CheckBox mStereoStream;
    @BindView(R.id.autoStart)
    protected CheckBox mAutoStartCheckBox;
    @BindView(R.id.print_debug_info)
    protected CheckBox mShowDebugInfoCheckBox;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.audio_demo_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    protected void loadParams(AudioStreamingActivity.AudioStreamConfig config, String url) {
        // initial value
        config.mAudioKBitrate = 48;
        config.mUrl = url;
        config.mStereoStream = false;
        config.mAudioEncodeProfile = AVConst.PROFILE_AAC_LOW;

        // audio bitrate
        if (!TextUtils.isEmpty(mAudioBitRateEditText.getText().toString())) {
            config.mAudioKBitrate = Integer.parseInt(mAudioBitRateEditText.getText().toString());
        }

        // audio encode profile
        switch (mAACProfileGroup.getCheckedRadioButtonId()) {
            case R.id.aac_he:
                config.mAudioEncodeProfile = AVConst.PROFILE_AAC_HE;
                break;
            case R.id.aac_he_v2:
                config.mAudioEncodeProfile = AVConst.PROFILE_AAC_HE_V2;
                break;
            case R.id.aac_lc:
            default:
                config.mAudioEncodeProfile = AVConst.PROFILE_AAC_LOW;
                break;
        }

        config.mStereoStream = mStereoStream.isChecked();
        config.mAutoStart = mAutoStartCheckBox.isChecked();
        config.mShowDebugInfo = mShowDebugInfoCheckBox.isChecked();
    }

    @Override
    public void start(String url) {
        AudioStreamingActivity.AudioStreamConfig config =
                new AudioStreamingActivity.AudioStreamConfig();
        loadParams(config, url);
        AudioStreamingActivity.startActivity(getActivity(), config, AudioStreamingActivity.class);
    }
}
