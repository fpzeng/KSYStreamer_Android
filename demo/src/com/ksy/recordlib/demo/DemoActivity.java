package com.ksy.recordlib.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ksy.recordlib.service.recorder.RecorderConstants;

public class DemoActivity extends Activity implements OnClickListener {

	private Button connectBT;
	private EditText urlET;
	private EditText frameRateET;
	private EditText videoBitRateET;
	private EditText audioBitRateET;
	private CheckBox resolutionCB;

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
		resolutionCB = (CheckBox) findViewById(R.id.resolution);
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

			if (!TextUtils.isEmpty(urlET.getText())
					&& urlET.getText().toString().startsWith("rtmp")) {
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

				if (resolutionCB.isChecked()) {
					videoResolution = RecorderConstants.VIDEO_RESOLUTION_MEDIUM;
				} else {
					videoResolution = RecorderConstants.VIDEO_RESOLUTION_LOW;
				}

				encodeWithHEVC = false;
					
				CameraActivity.startActivity(getApplicationContext(), 0, urlET.getText().toString(),
							frameRate,videoBitRate,audioBitRate,videoResolution,encodeWithHEVC);

			}

			break;
		default:
			break;
		}
	}

}
