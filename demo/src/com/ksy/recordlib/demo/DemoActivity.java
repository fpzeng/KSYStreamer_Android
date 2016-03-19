package com.ksy.recordlib.demo;//package com.ksy.recordlib.demo;
//

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ksy.recordlib.service.core.KSYStreamerConfig;
import com.ksy.recordlib.service.streamer.RecorderConstants;

public class DemoActivity extends Activity implements OnClickListener, RadioGroup.OnCheckedChangeListener {

    private Button connectBT;
    private EditText urlET;
    private EditText frameRateET;
    private EditText videoBitRateET;
    private EditText audioBitRateET;
    private RadioGroup resolutionCB;
    private RadioGroup encodeMethod;
    private RadioButton resolution360button;
    private RadioButton resolution480button;
    private RadioButton resolution540button;
    private RadioButton resolution720button;

    private RadioButton landscapeButton;
    private RadioButton portraitButton;

    private RadioButton softwareEncoding;
    private RadioButton hardwareEncoding;
    private RadioButton muteAudio;
    private CheckBox startPreviewAuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        connectBT = (Button) findViewById(R.id.connectBT);
        connectBT.setOnClickListener(this);

        urlET = (EditText) findViewById(R.id.rtmpUrl);

        frameRateET = (EditText) findViewById(R.id.frameRatePicker);
        frameRateET.setInputType(InputType.TYPE_CLASS_NUMBER);
        videoBitRateET = (EditText) findViewById(R.id.videoBitratePicker);
        videoBitRateET.setInputType(InputType.TYPE_CLASS_NUMBER);
        audioBitRateET = (EditText) findViewById(R.id.audioBitratePicker);
        audioBitRateET.setInputType(InputType.TYPE_CLASS_NUMBER);
        resolutionCB = (RadioGroup) findViewById(R.id.resolution_group);
        resolution360button = (RadioButton) findViewById(R.id.radiobutton1);
        resolution480button = (RadioButton) findViewById(R.id.radiobutton2);
        resolution540button = (RadioButton) findViewById(R.id.radiobutton3);
        resolution720button = (RadioButton) findViewById(R.id.radiobutton4);
        resolutionCB.setOnCheckedChangeListener(this);
        landscapeButton = (RadioButton) findViewById(R.id.orientationbutton1);
        portraitButton = (RadioButton) findViewById(R.id.orientationbutton2);
        softwareEncoding = (RadioButton) findViewById(R.id.encode_sw);
        hardwareEncoding = (RadioButton) findViewById(R.id.encode_hw);
        encodeMethod = (RadioGroup) findViewById(R.id.encode_group);
        muteAudio = (RadioButton) findViewById(R.id.mute_audio);
        startPreviewAuto = (CheckBox) findViewById(R.id.autoStart);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.connectBT:
                int frameRate = 0;
                int videoBitRate = 0;
                int audioBitRate = 0;
                int videoResolution = 0;
                boolean encodeWithHEVC = false;
                boolean landscape = false;
                boolean mute_audio = false;
                boolean startAuto = false;

                if (!TextUtils.isEmpty(urlET.getText())) {
//					&& urlET.getText().toString().startsWith("rtmp")) {
                    if (!TextUtils.isEmpty(frameRateET.getText().toString())) {
                        frameRate = Integer.parseInt(frameRateET.getText()
                                .toString());
                    }

                    if (!TextUtils.isEmpty(videoBitRateET.getText().toString())) {
                        videoBitRate = Integer.parseInt(videoBitRateET.getText()
                                .toString());
                    }

                    if (!TextUtils.isEmpty(audioBitRateET.getText().toString())) {
                        audioBitRate = Integer.parseInt(audioBitRateET.getText()
                                .toString());
                    }

                    if (resolution360button.isChecked()) {
                        videoResolution = RecorderConstants.VIDEO_RESOLUTION_360P;
                    } else if (resolution480button.isChecked()) {
                        videoResolution = RecorderConstants.VIDEO_RESOLUTION_480P;
                    } else if (resolution540button.isChecked()) {
                        videoResolution = RecorderConstants.VIDEO_RESOLUTION_540P;
                    } else {
                        videoResolution = RecorderConstants.VIDEO_RESOLUTION_720P;
                    }

                    encodeWithHEVC = false;
                    KSYStreamerConfig.ENCODE_METHOD encode_method;
                    if (hardwareEncoding.isChecked()) {
                        encode_method = KSYStreamerConfig.ENCODE_METHOD.HARDWARE;
                    } else {
                        encode_method = KSYStreamerConfig.ENCODE_METHOD.SOFTWARE;
                    }

                    if (landscapeButton.isChecked()) {
                        landscape = true;
                    } else {
                        landscape = false;
                    }
                    if (muteAudio.isChecked()) {
                        mute_audio = true;
                    } else {
                        mute_audio = false;
                    }
                    if (startPreviewAuto.isChecked()) {
                        startAuto = true;
                    } else {
                        startAuto = false;
                    }

                    CameraActivity.startActivity(getApplicationContext(), 0, urlET.getText().toString(),
                            frameRate, videoBitRate, audioBitRate, videoResolution, encodeWithHEVC, landscape, mute_audio, encode_method, startAuto);

                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

//

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
