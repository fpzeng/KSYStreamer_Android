package com.ksyun.media.streamer.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.StreamerConstants;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Base streaming activity.
 */

public class AudioStreamingActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String TAG = "BaseCameraActivity";

    protected static final int PERMISSION_REQUEST_AUDIOREC = 1;
    protected static final String START_STREAM = "开始直播";
    protected static final String STOP_STREAM = "停止直播";
    protected static final String START_RECORD = "开始录制";
    protected static final String STOP_RECORD = "停止录制";

    @BindView(R.id.chronometer)
    protected Chronometer mChronometer;
    @BindView(R.id.url)
    protected TextView mUrlTextView;
    @BindView(R.id.start_stream_tv)
    protected TextView mStreamingText;
    @BindView(R.id.start_record_tv)
    protected TextView mRecordingText;
    @BindView(R.id.debug_info)
    protected TextView mDebugInfoTextView;

    protected AudioStreamConfig mConfig;
    protected boolean mStreaming;
    protected boolean mRecording;
    protected boolean mIsChronometerStarted;
    protected boolean mDelayStartRecord;
    protected String mDebugInfo = "";
    protected String mSdcardPath = Environment.getExternalStorageDirectory().getPath();
    protected String mRecordUrl = mSdcardPath + "/rec_test.mp4";

    protected KSYStreamer mStreamer;
    protected Handler mMainHandler;
    protected Timer mTimer;

    public static class AudioStreamConfig {
        public String mUrl;
        public int mAudioKBitrate;
        public int mAudioEncodeProfile;
        public boolean mStereoStream;
        public boolean mAutoStart;
        public boolean mShowDebugInfo;

        public AudioStreamConfig fromJson(String json) {
            return new GsonBuilder().create().fromJson(json, this.getClass());
        }

        public String toJson() {
            return new GsonBuilder().create().toJson(this);
        }
    }

    public static void startActivity(Context context, AudioStreamConfig config, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("config", config.toJson());
        context.startActivity(intent);
    }

    protected AudioStreamConfig getConfig(Bundle bundle) {
        return new AudioStreamConfig().fromJson(bundle.getString("config"));
    }

    protected int getLayoutId() {
        return R.layout.audio_streaming_activity;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMainHandler = new Handler();
        mStreamer = new KSYStreamer(this);
        mConfig = getConfig(getIntent().getExtras());
        initUI();
        config();

        // 是否自动开始推流
        if (mConfig.mAutoStart) {
            startWithPermCheck(false);
        }
        // 是否显示调试信息
        if (mConfig.mShowDebugInfo) {
            startDebugInfoTimer();
        }
    }

    protected void initUI() {
        // empty here
    }

    protected void config() {
        // 设置推流URL地址
        if (!TextUtils.isEmpty(mConfig.mUrl)) {
            mUrlTextView.setText(mConfig.mUrl);
            mStreamer.setUrl(mConfig.mUrl);
        }

        // 设置音频码率
        if (mConfig.mAudioKBitrate > 0) {
            mStreamer.setAudioKBitrate(mConfig.mAudioKBitrate);
        }

        // 配置音频编码的profile
        mStreamer.setAudioEncodeProfile(mConfig.mAudioEncodeProfile);
        // 配置音频编码的声道数
        mStreamer.setAudioChannels(mConfig.mStereoStream ? 2 : 1);
        // 配置纯音频推流
        mStreamer.setAudioOnly(true);

        // 设置回调处理函数
        mStreamer.setOnInfoListener(mOnInfoListener);
        mStreamer.setOnErrorListener(mOnErrorListener);
    }

    protected void handleOnResume() {
        // 调用KSYStreamer的onResume接口
        mStreamer.onResume();
    }

    protected void handleOnPause() {
        // 调用KSYStreamer的onPause接口
        mStreamer.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleOnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handleOnPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理相关资源
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        mStreamer.release();
    }

    //start streaming
    protected void startStream() {
        mStreamer.startStream();
        mStreamingText.setText(STOP_STREAM);
        mStreamingText.postInvalidate();
        mStreaming = true;
    }

    // stop streaming
    protected void stopStream() {
        mStreamer.stopStream();
        mStreamingText.setText(START_STREAM);
        mStreamingText.postInvalidate();
        mStreaming = false;
        stopChronometer();
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

    private void startDebugInfoTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateDebugInfo();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDebugInfoTextView.setText(mDebugInfo);
                        }
                    });
                }
            }, 1000, 1000);
        }
    }

    // update debug info
    private void updateDebugInfo() {
        if (mStreamer == null) return;
        mDebugInfo = String.format(Locale.getDefault(), " " +
                        "RtmpHostIP()=%s DroppedFrameCount()=%d \n " +
                        "ConnectTime()=%dms DnsParseTime()=%dms \n " +
                        "UploadedKB()=%d EncodedFrames()=%d \n " +
                        "CurrentKBitrate=%d Version()=%s",
                mStreamer.getRtmpHostIP(), mStreamer.getDroppedFrameCount(),
                mStreamer.getConnectTime(), mStreamer.getDnsParseTime(),
                mStreamer.getUploadedKBytes(), mStreamer.getEncodedFrames(),
                mStreamer.getCurrentUploadKBitrate(), KSYStreamer.getVersion());
    }

    @OnClick(R.id.start_stream_tv)
    protected void onStartStreamClick() {
        if (mStreaming) {
            stopStream();
        } else {
            startWithPermCheck(false);
        }
    }

    @OnClick(R.id.start_record_tv)
    protected void onStartRecordClick() {
        if (mRecording) {
            stopRecord();
        } else {
            startWithPermCheck(true);
        }
    }

    protected void startChronometer() {
        if (!mIsChronometerStarted) {
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mIsChronometerStarted = true;
        }
    }

    protected void stopChronometer() {
        if (mStreaming || mRecording) {
            return;
        }
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
        mIsChronometerStarted = false;
    }

    protected void onStreamerInfo(int what, int msg1, int msg2) {
        Log.d(TAG, "OnInfo: " + what + " msg1: " + msg1 + " msg2: " + msg2);
        switch (what) {
            case StreamerConstants.KSY_STREAMER_OPEN_STREAM_SUCCESS:
                Log.d(TAG, "KSY_STREAMER_OPEN_STREAM_SUCCESS");
                mStreamingText.setText(STOP_STREAM);
                startChronometer();
                break;
            case StreamerConstants.KSY_STREAMER_FRAME_SEND_SLOW:
                Log.d(TAG, "KSY_STREAMER_FRAME_SEND_SLOW " + msg1 + "ms");
                Toast.makeText(AudioStreamingActivity.this, "Network not good!",
                        Toast.LENGTH_SHORT).show();
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

    protected void onStreamerError(int what, int msg1, int msg2) {
        Log.e(TAG, "streaming error: what=" + what + " msg1=" + msg1 + " msg2=" + msg2);
        switch (what) {
            case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
            case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                Toast.makeText(AudioStreamingActivity.this,
                        "Audio capture failed!",
                        Toast.LENGTH_SHORT).show();
                stopStream();
                break;
            default:
                reStreaming(what);
                break;
        }
    }

    protected void reStreaming(int err) {
        switch (err) {
            case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_CLOSE_FAILED:
            case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_ERROR_UNKNOWN:
            case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_OPEN_FAILED:
            case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_FORMAT_NOT_SUPPORTED:
            case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_WRITE_FAILED:
                // just stop record without retry.
                stopRecord();
                mRecordingText.setText(START_RECORD);
                mRecordingText.postInvalidate();
                mRecording = false;
                stopChronometer();
                break;
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

    private KSYStreamer.OnInfoListener mOnInfoListener = new KSYStreamer.OnInfoListener() {
        @Override
        public void onInfo(int what, int msg1, int msg2) {
            onStreamerInfo(what, msg1, msg2);
        }
    };

    private KSYStreamer.OnErrorListener mOnErrorListener = new KSYStreamer.OnErrorListener() {
        @Override
        public void onError(int what, int msg1, int msg2) {
            onStreamerError(what, msg1, msg2);
        }
    };

    protected void startWithPermCheck(boolean startRecord) {
        int audioPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (audioPerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.e(TAG, "No AudioRecord permission, please check");
                Toast.makeText(this, "No AudioRecord permission, please check",
                        Toast.LENGTH_LONG).show();
            } else {
                mDelayStartRecord = startRecord;
                String[] permissions = {Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE};
                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSION_REQUEST_AUDIOREC);
            }
        } else {
            if (startRecord) {
                startRecord();
            } else {
                startStream();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_AUDIOREC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mDelayStartRecord) {
                        startRecord();
                    } else {
                        startStream();
                    }
                } else {
                    Log.e(TAG, "No AudioRecord permission");
                    Toast.makeText(this, "No AudioRecord permission",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}
