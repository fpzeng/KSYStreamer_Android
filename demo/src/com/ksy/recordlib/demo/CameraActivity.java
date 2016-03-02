package com.ksy.recordlib.demo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.ksy.recordlib.service.core.KSYStreamer;
import com.ksy.recordlib.service.core.KSYStreamerConfig;
import com.ksy.recordlib.service.recorder.RecorderConstants;

public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";

	private GLSurfaceView mCameraPreview;

	private KSYStreamer mStreamer;

	private Handler mHandler;

	private final ButtonObserver mObserverButton = new ButtonObserver();

	private Chronometer chronometer;
	private View mDeleteView;
	private View mSwitchCameraView;
	private View mFlashView;
	private TextView mShootingText;
	private boolean recording = false;
	private boolean isFlashOpened = false;
	private String mUrl;
	private static final String START_STRING = "开始直播";
	private static final String STOP_STRING = "停止直播";
	private TextView mUrlTextView;
	private volatile boolean mAcitivityResumed = false;

	public final static String URL = "url";
	public final static String FRAME_RATE = "framerate";
	public final static String INIT_VIDEO_BITRATE = "init_video_bitrate";
	public final static String MAX_VIDEO_BITRATE = "max_video_bitrate";
	public final static String MIN_VIDEO_BITRATE = "min_video_bitrate";
	public final static String AUDIO_BITRATE = "audio_bitrate";
	public final static String VIDEO_RESOLUTION = "video_resolution";
	public final static String EncodeWithHEVC = "encode_with_hevc";
	public final static String LANDSCAPE = "landscape";

	public static void startActivity(Context context, int fromType,
			String rtmpUrl, int frameRate, int initVideoBitrate, int maxVideoBitrate, int minVideoBitrate, int audioBitrate,
			int videoResolution, boolean encodeWithHEVC, boolean isLandscape) {
		Intent intent = new Intent(context, CameraActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("type", fromType);
		intent.putExtra(URL, rtmpUrl);
		intent.putExtra(FRAME_RATE, frameRate);
		intent.putExtra(INIT_VIDEO_BITRATE, initVideoBitrate);
		intent.putExtra(MAX_VIDEO_BITRATE, maxVideoBitrate);
		intent.putExtra(MIN_VIDEO_BITRATE, minVideoBitrate);
		intent.putExtra(AUDIO_BITRATE, audioBitrate);
		intent.putExtra(VIDEO_RESOLUTION, videoResolution);
		intent.putExtra(EncodeWithHEVC, encodeWithHEVC);
		intent.putExtra(LANDSCAPE, isLandscape);
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
		mUrlTextView = (TextView) findViewById(R.id.url);

		KSYStreamerConfig.Builder builder = new KSYStreamerConfig.Builder();
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg != null && msg.obj != null) {
					String content = msg.obj.toString();
					switch (msg.what) {
						case RecorderConstants.KSYVIDEO_OPEN_FILE_FAILED:
						case RecorderConstants.KSYVIDEO_ENCODED_FRAMES_FAILED:
						case RecorderConstants.KSYVIDEO_WRITE_FRAME_FAILED:
							Toast.makeText(CameraActivity.this, content,
									Toast.LENGTH_LONG).show();
							mShootingText.setText(START_STRING);
							mShootingText.postInvalidate();
							break;
						case RecorderConstants.KSYVIDEO_OPEN_STREAM_SUCC:
							chronometer.setBase(SystemClock.elapsedRealtime());
							// 开始计时
							chronometer.start();
							mShootingText.setText(STOP_STRING);
							mShootingText.postInvalidate();
							break;
						case RecorderConstants.KSYVIDEO_ENCODED_FRAMES_THRESHOLD:
							chronometer.stop();
							recording = false;
							mShootingText.setText(START_STRING);
							mShootingText.postInvalidate();
							Toast.makeText(CameraActivity.this, content,
									Toast.LENGTH_LONG).show();
							break;
						default:
							Toast.makeText(CameraActivity.this, content,
									Toast.LENGTH_LONG).show();
					}
				}
			}

		};

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String url = bundle.getString(URL);
			if (!TextUtils.isEmpty(url)) {
				builder.setmUrl(url);
				mUrl = url;
				mUrlTextView.setText(mUrl);
			}

			int frameRate = bundle.getInt(FRAME_RATE, 0);
			if (frameRate > 0) {
				builder.setFrameRate(frameRate);
			}

			int initVideoBitrate = bundle.getInt(INIT_VIDEO_BITRATE, 0);
			if (initVideoBitrate > 0) {
				builder.setInitVideoBitrate(initVideoBitrate);
			}
			
			int maxVideoBitrate = bundle.getInt(MAX_VIDEO_BITRATE, 0);
			if (maxVideoBitrate > 0) {
				builder.setMaxVideoBitrate(maxVideoBitrate);
			}
			
			int minVideoBitrate = bundle.getInt(MIN_VIDEO_BITRATE, 0);
			if (minVideoBitrate > 0) {
				builder.setMinVideoBitrate(minVideoBitrate);
			}

			int audioBitrate = bundle.getInt(AUDIO_BITRATE, 0);
			if (audioBitrate > 0) {
				builder.setAudioBitrate(audioBitrate);
			}

			int videoResolution = bundle.getInt(VIDEO_RESOLUTION, 0);
			builder.setVideoResolution(videoResolution);

			String timeSec = String.valueOf(System.currentTimeMillis() / 1000);
			// ---------不合法！请联系我们申请
			String skSign = md5("IVALID_SK" + timeSec);
			builder.setAppId("INVALID_APP_ID");
			builder.setAccessKey("IVALID_AK");
			// ----------
			builder.setSecretKeySign(skSign);
			builder.setTimeSecond(timeSec);
			
			boolean landscape = bundle.getBoolean(LANDSCAPE, false);
			builder.setDefaultLandscape(landscape);
			if (landscape) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}

		}

		mStreamer = new KSYStreamer(this);
		mStreamer.setDisplayPreview(mCameraPreview);
		mStreamer.setConfig(builder.build());
		mStreamer.setOnStatusListener(mOnErrorListener);
		mStreamer.enableDebugLog(false);

		mShootingText = (TextView) findViewById(R.id.click_to_shoot);
		mShootingText.setClickable(true);
		mShootingText.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (recording) {
					if (mStreamer.stopStream()) {
						chronometer.stop();
						mShootingText.setText(START_STRING);
						mShootingText.postInvalidate();
						recording = false;
					} else {
						Log.e(TAG,"操作太频繁");
					}
				} else {
					if (mStreamer.startStream()) {
						mShootingText.setText(STOP_STRING);
						mShootingText.postInvalidate();
						recording = true;
					} else {
						Log.e(TAG,"操作太频繁");
					}
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

		chronometer = (Chronometer) this.findViewById(R.id.chronometer);

	}

	@Override
	public void onResume() {
		super.onResume();
		mStreamer.onResume();
		mAcitivityResumed = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		mStreamer.onPause();
		mAcitivityResumed = false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			new AlertDialog.Builder(CameraActivity.this).setCancelable(true)
			.setTitle("结束直播?")
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {

				}
			})
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					mStreamer.stopStream();
					chronometer.stop();
					recording = false;
					CameraActivity.this.finish();

				}
			}).show();
			break;

		default:
			break;
		}
		return true;
	}

	private boolean failed = false;

	public KSYStreamer.OnStatusListener mOnErrorListener = new KSYStreamer.OnStatusListener() {
		@Override
		public void onStatus(int what, int arg1, int arg2, String msg) {
			// msg may be null
			Log.d(TAG,
					"------------mOnErrorListener callback: onError, what = "
							+ what);
			switch (what) {
			case RecorderConstants.KSYVIDEO_OPEN_STREAM_SUCC:
				// 推流成功
				Log.d(TAG, "KSYVIDEO_OPEN_STREAM_SUCC");
				mHandler.obtainMessage(what, "start stream succ")
						.sendToTarget();
				break;
			case RecorderConstants.KSYVIDEO_ENCODED_FRAMES_THRESHOLD:
				//认证失败且超过编码上限
				Log.d(TAG, "KSYVIDEO_ENCODED_FRAME_THRESHOLD");
				mHandler.obtainMessage(what, "KSYVIDEO_ENCODED_FRAME_THRESHOLD")
						.sendToTarget();
				break;
			case RecorderConstants.KSYVIDEO_AUTH_FAILED:
				//认证失败
				Log.d(TAG, "KSYVIDEO_AUTH_ERROR");
				break;
			case RecorderConstants.KSYVIDEO_ENCODED_FRAMES_FAILED:
				//编码失败
				Log.e(TAG, "---------KSYVIDEO_ENCODED_FRAMES_FAILED");
				break;
			case RecorderConstants.KSYVIDEO_NETWORK_NOT_GOOD:
				//网络状况不佳
				if (mHandler != null) {
					mHandler.obtainMessage(what, "network not good").sendToTarget();
				}
				break;
			case RecorderConstants.KSYVIDEO_EST_BW_DROP:
				//开始下调目标码率
				Log.e(TAG, "---------KSYVIDEO_EST_BW_DROP");
				break;
			case RecorderConstants.KSYVIDEO_EST_BW_RAISE:
				//开始上调目标码率
				Log.e(TAG, "---------KSYVIDEO_EST_BW_RAISE");
				break;
			default:
				if (msg != null) {
					// 可以在这里处理断网重连的逻辑
					if (TextUtils.isEmpty(mUrl)) {
						mStreamer
								.updateUrl("rtmp://test.uplive.ksyun.com/live/androidtest");
					} else {
						mStreamer.updateUrl(mUrl);
					}
					new Thread(new Runnable() {

						@Override
						public void run() {
							boolean needReconnect = true;
							while (needReconnect) {
								try {
									Thread.sleep(2000); 
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								//只在Activity对用户可见时重连
								if (mAcitivityResumed) {
									if (mStreamer.startStream()) {
										recording = true;
										needReconnect = false;
									}
								}
							}
						}

					}).start();
				}
				if (mHandler != null) {
					mHandler.obtainMessage(what, msg).sendToTarget();
				}
			}
		}

	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		mStreamer.onDestroy();
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
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

		new AlertDialog.Builder(CameraActivity.this).setCancelable(true)
				.setTitle("结束直播?")
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

					}
				})
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						mStreamer.stopStream();
						chronometer.stop();
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

	private String md5(String string) {
		byte[] hash;
		try {
			hash = MessageDigest.getInstance("MD5").digest(string.getBytes());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Huh, MD5 should be supported?", e);
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}

		return hex.toString();
	}

}
