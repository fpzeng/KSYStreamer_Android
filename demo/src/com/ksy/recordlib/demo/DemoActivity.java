package com.ksy.recordlib.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ksy.recordlib.service.recorder.RecorderConstants;

public class DemoActivity extends Activity implements OnClickListener {

	private Button connectBT;
	private EditText urlET;
	private EditText frameRateET;
	private EditText initVideoBitrateET;
	private EditText maxVideoBitrateET;
	private EditText minVideoBitrateET;
	private EditText audioBitRateET;
	private RadioGroup resolutionCB;
	private RadioButton resolution360button;
	private RadioButton resolution480button;
	private RadioButton resolution540button;
	private RadioButton resolution720button;
	
	private RadioButton landscapeButton;
	private RadioButton portraitButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_activity);

		connectBT = (Button) findViewById(R.id.connectBT);
		connectBT.setOnClickListener(this);

		urlET = (EditText) findViewById(R.id.rtmpUrl);

		frameRateET = (EditText) findViewById(R.id.frameRatePicker);
		frameRateET.setInputType(InputType.TYPE_CLASS_NUMBER);
		initVideoBitrateET = (EditText) findViewById(R.id.initVideoBitratePicker);
		initVideoBitrateET.setInputType(InputType.TYPE_CLASS_NUMBER);
		maxVideoBitrateET = (EditText) findViewById(R.id.maxVideoBitratePicker);
		maxVideoBitrateET.setInputType(InputType.TYPE_CLASS_NUMBER);
		minVideoBitrateET = (EditText) findViewById(R.id.minVideoBitratePicker);
		minVideoBitrateET.setInputType(InputType.TYPE_CLASS_NUMBER);
		audioBitRateET = (EditText) findViewById(R.id.audioBitratePicker);
		audioBitRateET.setInputType(InputType.TYPE_CLASS_NUMBER);
		resolutionCB = (RadioGroup) findViewById(R.id.resolution_group);
		resolution360button = (RadioButton) findViewById(R.id.radiobutton1);
		resolution480button = (RadioButton) findViewById(R.id.radiobutton2);
		resolution540button = (RadioButton) findViewById(R.id.radiobutton3);
		resolution720button = (RadioButton) findViewById(R.id.radiobutton4);
		
		landscapeButton = (RadioButton) findViewById(R.id.orientationbutton1);
		portraitButton = (RadioButton) findViewById(R.id.orientationbutton2);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.connectBT:
			int frameRate = 0;
			int initVideoBitrate = 300;
			int maxVideoBitrate = 500;
			int minVideoBitrate = 200;
			int audioBitRate = 0;
			int videoResolution = 0;
			boolean encodeWithHEVC = false;
			boolean landscape = false;

			if (!TextUtils.isEmpty(urlET.getText())) {
//					&& urlET.getText().toString().startsWith("rtmp")) {
				if (!TextUtils.isEmpty(frameRateET.getText().toString())) {
					frameRate = Integer.parseInt(frameRateET.getText()
							.toString());
				}

				if (!TextUtils.isEmpty(initVideoBitrateET.getText().toString())) {
					initVideoBitrate = Integer.parseInt(initVideoBitrateET.getText()
							.toString());
				}
				
				if (!TextUtils.isEmpty(maxVideoBitrateET.getText().toString())) {
					maxVideoBitrate = Integer.parseInt(maxVideoBitrateET.getText()
							.toString());
				}
				
				if (!TextUtils.isEmpty(minVideoBitrateET.getText().toString())) {
					minVideoBitrate = Integer.parseInt(minVideoBitrateET.getText()
							.toString());
				}

				if (!TextUtils.isEmpty(audioBitRateET.getText().toString())) {
					audioBitRate = Integer.parseInt(audioBitRateET.getText()
							.toString());
				}

				if (resolution360button.isChecked()) {
					videoResolution = RecorderConstants.VIDEO_RESOLUTION_360P;
				} else if (resolution480button.isChecked()){
					videoResolution = RecorderConstants.VIDEO_RESOLUTION_480P;
				} else if (resolution540button.isChecked()) {
					videoResolution = RecorderConstants.VIDEO_RESOLUTION_540P;
				} else {
					videoResolution = RecorderConstants.VIDEO_RESOLUTION_720P;
				}

				encodeWithHEVC = false;
				
				if (landscapeButton.isChecked()) {
					landscape = true;
				} else {
					landscape = false;
				}
					
				CameraActivity.startActivity(getApplicationContext(), 0, urlET.getText().toString(),
							frameRate,initVideoBitrate,maxVideoBitrate, minVideoBitrate, audioBitRate,videoResolution,encodeWithHEVC, landscape);

			}

			break;
		default:
			break;
		}
	}

}
