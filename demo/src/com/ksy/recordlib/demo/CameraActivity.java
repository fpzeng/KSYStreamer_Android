package com.ksy.recordlib.demo;



import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ksy.recordlib.service.core.KSYStreamer;
import com.ksy.recordlib.service.core.KSYStreamerConfig;
import com.ksy.recordlib.service.recorder.RecorderConstants;



public class CameraActivity extends Activity{

	private static final String TAG = "CameraActivity";

	private GLSurfaceView mCameraPreview;

	private KSYStreamer mStreamer;
	
	private Handler mHandler;

	private final ButtonObserver mObserverButton = new ButtonObserver();

	private View mDeleteView;
	private View mSwitchCameraView;
	private View mFlashView;
	private TextView mShootingText;
	private boolean recording = false;
	private boolean isFlashOpened = false;
	private String mUrl;
	
	
	public final static String URL = "url";
	public final static String FRAME_RATE = "framerate";
	public final static String VIDEO_BITRATE = "video_bitrate";
	public final static String AUDIO_BITRATE = "audio_bitrate";
	public final static String VIDEO_RESOLUTION = "video_resolution";
	public final static String EncodeWithHEVC = "encode_with_hevc";

	public static void startActivity(Context context, int fromType, String rtmpUrl, int frameRate,
			int videoBitrate,int audioBitrate, int videoResolution, boolean encodeWithHEVC) {
		Intent intent = new Intent(context, CameraActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("type", fromType);
		intent.putExtra(URL, rtmpUrl);
		intent.putExtra(FRAME_RATE, frameRate);
		intent.putExtra(VIDEO_BITRATE, videoBitrate);
		intent.putExtra(AUDIO_BITRATE, audioBitrate);
		intent.putExtra(VIDEO_RESOLUTION, videoResolution);
		intent.putExtra(EncodeWithHEVC, encodeWithHEVC);
		context.startActivity(intent);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.camera_activity);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mCameraPreview = (GLSurfaceView) findViewById(R.id.camera_preview);

		KSYStreamerConfig.Builder builder = new KSYStreamerConfig.Builder();
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg != null && msg.obj != null) {
					String content = msg.obj.toString();
					switch(msg.what) {
						case RecorderConstants.KSYVIDEO_OPEN_FILE_FAILED:
						case RecorderConstants.KSYVIDEO_ENCODED_FRAMES_FAILED:
						case RecorderConstants.KSYVIDEO_WRITE_FRAME_FAILED:
							Toast.makeText(CameraActivity.this, content, Toast.LENGTH_LONG).show();
//							mRecordClient.stop();
							mShootingText.setText(new String("开始直播"));
							mShootingText.postInvalidate();
							break;
						case RecorderConstants.KSYVIDEO_OPEN_STREAM_SUCC:
							mShootingText.setText(new String("停止直播"));
							mShootingText.postInvalidate();
							break;
						default:
							Toast.makeText(CameraActivity.this, content, Toast.LENGTH_LONG).show();				
					};
				}
			}
			
		};

		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String url = bundle.getString(URL);
			if (!TextUtils.isEmpty(url)) {
				builder.setmUrl(url);
				mUrl = url;
			}
			
			int frameRate = bundle.getInt(FRAME_RATE, 0);
			if (frameRate > 0) {
				builder.setFrameRate(frameRate);
			}
			
			int videoBitrate = bundle.getInt(VIDEO_BITRATE, 0);
			if (videoBitrate > 0) {
				builder.setVideoBitrate(videoBitrate);
			}
			
			int audioBitrate = bundle.getInt(AUDIO_BITRATE, 0);
			if (audioBitrate > 0) {
				builder.setAudioBitrate(audioBitrate);
			}
			
			int videoResolution = bundle.getInt(VIDEO_RESOLUTION, 0);
			builder.setVideoResolution(videoResolution);
			
			String timeSec = String.valueOf(System.currentTimeMillis() / 1000);
			//---------不合法！请联系我们申请
			String skSign = md5("KSYVIDEOSecretKey" + timeSec);
			builder.setAppId("KSYVIDEOAppId");
			builder.setAccessKey("KSYVIDEOAccessKey");
			//----------
			builder.setSecretKeySign(skSign);
			builder.setTimeSecond(timeSec);
			
		}
		
		mStreamer = new KSYStreamer(this);
		mStreamer.setDisplayPreview(mCameraPreview);
		mStreamer.setConfig(builder.build());
		mStreamer.setOnStatusListener(mOnErrorListener);
		mStreamer.enableDebugLog(false);

		mShootingText = (TextView)findViewById(R.id.click_to_shoot);
		mShootingText.setClickable(true);
		mShootingText.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (recording) {
					mStreamer.stopStream();
					mShootingText.setText(new String("开始直播"));
					mShootingText.postInvalidate();
					recording = false;
				} else {
					mStreamer.startStream();
					mShootingText.setText(new String("停止直播"));
					mShootingText.postInvalidate();
					recording = true;
				}
				
			}
		});


		mDeleteView = findViewById(R.id.backoff);
		mDeleteView.setOnClickListener(mObserverButton);
		mDeleteView.setEnabled(true);
		
		mSwitchCameraView = findViewById(R.id.switch_cam);
		mSwitchCameraView.setOnClickListener(mObserverButton);
		mSwitchCameraView.setEnabled(true);

		mFlashView = findViewById(R.id.flash);
		mFlashView.setOnClickListener(mObserverButton);
		mFlashView.setEnabled(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		mStreamer.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
		mStreamer.onPause();
	}

	private boolean failed = false;
	
	public KSYStreamer.OnStatusListener mOnErrorListener = new KSYStreamer.OnStatusListener() {
		@Override
		public void onStatus(int what, int arg1, int arg2, String msg) {
			//msg may be null
			Log.d(TAG, "------------mOnErrorListener callback: onError, what = " + what);
			switch (what)
			{
				case RecorderConstants.KSYVIDEO_OPEN_STREAM_SUCC:
					//推流成功
					Log.d(TAG, "KSYVIDEO_OPEN_STREAM_SUCC");
					mHandler.obtainMessage(what,"start stream succ").sendToTarget();
					break;
				case RecorderConstants.KSYVIDEO_ENCODED_FRAMES_THRESHOLD:
					Log.d(TAG, "KSYVIDEO_ENCODED_FRAME_THRESHOLD");
					mHandler.obtainMessage(what, "KSYVIDEO_ENCODED_FRAME_THRESHOLD").sendToTarget();
					break;
				case RecorderConstants.KSYVIDEO_AUTH_FAILED:
					Log.d(TAG, "KSYVIDEO_AUTH_ERROR");
					break;
				case RecorderConstants.KSYVIDEO_ENCODED_FRAMES_FAILED:
					Log.e(TAG,"---------KSYVIDEO_ENCODED_FRAMES_FAILED");
					break;
				case RecorderConstants.KSYVIDEO_NETWORK_NOT_GOOD:
					mHandler.obtainMessage(what, "network not good").sendToTarget();
					break;
				default:
					if (msg != null) {
							//可以在这里处理断网重连的逻辑
							if (TextUtils.isEmpty(mUrl)) {
								mStreamer.updateUrl("rtmp://120.132.75.127/demo/androidtest");
							} else {
								mStreamer.updateUrl(mUrl);
							}
							new Thread(new Runnable() {
		
								@Override
								public void run() {
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									mStreamer.startStream();	
								}
								
							}).start();
						}
						mHandler.obtainMessage(what,msg).sendToTarget();
//					}
			}
		}
		
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mStreamer.onDestroy();
	}



	protected int mStopTime = 0;


	private boolean clearState() {
		if (clearBackoff()) {
			return true;
		}
		return false;
	}

	private long lastClickTime = 0;

	private void onSwitchCamClick() {
		long curTime = System.currentTimeMillis();
		if (curTime - lastClickTime < 1000) {
			return;
		}
		lastClickTime = curTime;

		if (clearState()) {
			return;
		}
		mStreamer.switchCamera();
	}
	
	private void onFlashClick() {
		if (isFlashOpened) {
			mStreamer.toggleTorch(false);
			isFlashOpened = false;
		} else {
			mStreamer.toggleTorch(true);
			isFlashOpened = true;			
		}
	}

	private boolean clearBackoff() {
		if (mDeleteView.isSelected()) {
			mDeleteView.setSelected(false);
			return true;
		}
		return false;
	}

	private void onBackoffClick() {
		

			new AlertDialog.Builder(CameraActivity.this).setCancelable(true).setTitle("结束直播?").setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
								
				}
			}).setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					mStreamer.stopStream();
					recording = false;
					CameraActivity.this.finish();
					
				}
			}).show();
	}


	private class ButtonObserver implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.switch_cam:
				onSwitchCamClick();
				break;
			case R.id.backoff:
				onBackoffClick();
				break;
			case R.id.flash:
				onFlashClick();
				break;
			default:
				break;
			}
		}
	}

	private String md5(String string)
	{
		byte[] hash;
		try {
			hash = MessageDigest.getInstance("MD5").digest(string.getBytes());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Huh, MD5 should be supported?", e);
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10) hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}

		return hex.toString();
	}
	
}
