package com.ksy.recordlib.demo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ksy.recordlib.service.core.KSYStreamer;
import com.ksy.recordlib.service.core.KSYStreamerConfig;
import com.ksy.recordlib.service.hardware.ksyfilter.KSYImageFilter;
import com.ksy.recordlib.service.stats.OnLogEventListener;
import com.ksy.recordlib.service.streamer.OnPreviewFrameListener;
import com.ksy.recordlib.service.streamer.OnStatusListener;
import com.ksy.recordlib.service.streamer.RecorderConstants;
import com.ksy.recordlib.service.util.audio.KSYBgmPlayer;
import com.ksy.recordlib.service.util.audio.OnAudioRawDataListener;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";


    private GLSurfaceView mCameraPreview;

    private KSYStreamer mStreamer;

    private KSYMediaPlayer mKsyMediaPlayer;

    private KSYBgmPlayer mKsyBgmPlayer;

    private Handler mHandler;

    Bitmap mPipBitmap;

    Bitmap mBitmap;

    private final ButtonObserver mObserverButton = new ButtonObserver();

    private Chronometer chronometer;
    private View mDeleteView;
    private View mSwitchCameraView;
    private View mFlashView;
    private CheckBox enable_beauty;
    private TextView mPip;
    private TextView mPicturePip;
    private CheckBox mBgm;
    private CheckBox mEarMirror;
    private CheckBox mMuteAudio;
    private CheckBox mWaterMark;
    private TextView mShootingText;
    private boolean recording = false;
    private boolean isFlashOpened = false;
    private boolean startAuto = false;
    private boolean audio_mix = false;
    private boolean mute_audio = false;
    private boolean earMirror = false;
    private boolean showWaterMark = false;
    private boolean landscape = false;
    private boolean printDebugInfo = false;
    private boolean mPipMode = false;
    private boolean mPicPipMode = false;
    private long lastPipClickTime = 0;
    private float pipVolume = 0.6f;
    private float bgmVolume = 0.6f;
    private String mUrl, mDebugInfo = "";
    private String mBgmPath = "/sdcard/test.mp3";
    private String mLogoPath = "/sdcard/test.png";
    private String mPipPath = "/sdcard/test.mp4";
    private static final String START_STRING = "开始直播";
    private static final String STOP_STRING = "停止直播";
    private TextView mUrlTextView, mDebugInfoTextView;
    private volatile boolean mAcitivityResumed = false;
    private KSYStreamerConfig.ENCODE_METHOD encode_method = KSYStreamerConfig.ENCODE_METHOD.SOFTWARE;
    public final static String URL = "url";
    public final static String FRAME_RATE = "framerate";
    public final static String VIDEO_BITRATE = "video_bitrate";
    public final static String AUDIO_BITRATE = "audio_bitrate";
    public final static String VIDEO_RESOLUTION = "video_resolution";
    public final static String EncodeWithHEVC = "encode_with_hevc";
    public final static String LANDSCAPE = "landscape";
    public final static String ENCDODE_METHOD = "ENCDODE_METHOD";
    public final static String START_ATUO = "start_auto";
    public final static String FRONT_CAMERA_MIRROR = "front_camera_mirror";
    public final static String TEST_SW_FILTER = "testSWFilterInterface";
    public final static String MANUAL_FOCUS = "manual_focus";
    public static final String SHOW_DEBUGINFO = "SHOW_DEBUGINFO";

    Timer timer;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    private int frameCount;
    private boolean testSWFilterInterface;

    public static void startActivity(Context context, int fromType,
                                     String rtmpUrl, int frameRate, int videoBitrate, int audioBitrate,
                                     int videoResolution, boolean encodeWithHEVC, boolean isLandscape, boolean isFrontCameraMirror, KSYStreamerConfig.ENCODE_METHOD encodeMethod, boolean startAuto,
                                     boolean testSWFilterInterface, boolean manualFocus, boolean showDebugInfo) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type", fromType);
        intent.putExtra(URL, rtmpUrl);
        intent.putExtra(FRAME_RATE, frameRate);
        intent.putExtra(VIDEO_BITRATE, videoBitrate);
        intent.putExtra(AUDIO_BITRATE, audioBitrate);
        intent.putExtra(VIDEO_RESOLUTION, videoResolution);
        intent.putExtra(EncodeWithHEVC, encodeWithHEVC);
        intent.putExtra(LANDSCAPE, isLandscape);
        intent.putExtra(ENCDODE_METHOD, encodeMethod);
        intent.putExtra(START_ATUO, startAuto);
        intent.putExtra(FRONT_CAMERA_MIRROR, isFrontCameraMirror);
        intent.putExtra(TEST_SW_FILTER, testSWFilterInterface);
        intent.putExtra(MANUAL_FOCUS, manualFocus);
        intent.putExtra(SHOW_DEBUGINFO, showDebugInfo);
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
        enable_beauty = (CheckBox) findViewById(R.id.click_to_switch_beauty);
        mPip = (TextView) findViewById(R.id.pip);
        mPip.setClickable(true);
        mPicturePip = (TextView) findViewById(R.id.picture_pip);
        mPicturePip.setClickable(true);
        mBgm = (CheckBox) findViewById(R.id.bgm);
        mEarMirror = (CheckBox) findViewById(R.id.ear_mirror);
        mMuteAudio = (CheckBox) findViewById(R.id.mute);
        mWaterMark = (CheckBox) findViewById(R.id.watermark);

        audio_mix = mBgm.isChecked();
        earMirror = mEarMirror.isChecked();
        mute_audio = mMuteAudio.isChecked();
        showWaterMark = mWaterMark.isChecked();

        KSYStreamerConfig.Builder builder = new KSYStreamerConfig.Builder();
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg != null && msg.obj != null) {
                    String content = msg.obj.toString();
                    switch (msg.what) {
                        case RecorderConstants.KSYVIDEO_CONNECT_FAILED:
                        case RecorderConstants.KSYVIDEO_ENCODED_FRAMES_FAILED:
                        case RecorderConstants.KSYVIDEO_CONNECT_BREAK:
                            Toast.makeText(CameraActivity.this, content,
                                    Toast.LENGTH_LONG).show();
                            chronometer.stop();
                            mShootingText.setText(START_STRING);
                            mShootingText.postInvalidate();
                            break;
                        case RecorderConstants.KSYVIDEO_OPEN_STREAM_SUCC:
                            chronometer.setBase(SystemClock.elapsedRealtime());
                            // 开始计时
                            chronometer.start();
                            mShootingText.setText(STOP_STRING);
                            mShootingText.postInvalidate();
                            beginInfoUploadTimer();
                            break;
                        case RecorderConstants.KSYVIDEO_ENCODED_FRAMES_THRESHOLD:
                            chronometer.stop();
                            recording = false;
                            mShootingText.setText(START_STRING);
                            mShootingText.postInvalidate();
                            Toast.makeText(CameraActivity.this, content,
                                    Toast.LENGTH_LONG).show();
                            break;
                        case RecorderConstants.KSYVIDEO_INIT_DONE:
                            if (mShootingText != null)
                                mShootingText.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "初始化完成", Toast.LENGTH_SHORT).show();
//							if(!checkoutPreviewStarted()){
//								return;
//							}
                            checkPermission();
                            if (startAuto && mStreamer.startStream()) {
                                mShootingText.setText(STOP_STRING);
                                mShootingText.postInvalidate();
                                recording = true;
                            }
                            break;
                        default:
                            Toast.makeText(CameraActivity.this, content,
                                    Toast.LENGTH_SHORT).show();
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

            int videoBitrate = bundle.getInt(VIDEO_BITRATE, 0);
            if (videoBitrate > 0) {
                //设置最高码率，即目标码率
                builder.setMaxAverageVideoBitrate(videoBitrate);
                //设置最低码率
                builder.setMinAverageVideoBitrate(videoBitrate * 2 / 8);
                //设置初始码率
                builder.setInitAverageVideoBitrate(videoBitrate * 5 / 8);
            }

            int audioBitrate = bundle.getInt(AUDIO_BITRATE, 0);
            if (audioBitrate > 0) {
                builder.setAudioBitrate(audioBitrate);
            }

            int videoResolution = bundle.getInt(VIDEO_RESOLUTION, 0);
            builder.setVideoResolution(videoResolution);

            encode_method = (KSYStreamerConfig.ENCODE_METHOD) bundle.get(ENCDODE_METHOD);
            builder.setEncodeMethod(encode_method);

            builder.setSampleAudioRateInHz(44100);
            builder.setEnableStreamStatModule(true);

            landscape = bundle.getBoolean(LANDSCAPE, false);
            builder.setDefaultLandscape(landscape);

            if (landscape) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            startAuto = bundle.getBoolean(START_ATUO, false);

            boolean isFrontCameraMirror = bundle.getBoolean(FRONT_CAMERA_MIRROR, false);
            builder.setFrontCameraMirror(isFrontCameraMirror);
            testSWFilterInterface = bundle.getBoolean(TEST_SW_FILTER, false);
            boolean focus_manual = bundle.getBoolean(MANUAL_FOCUS, false);
            builder.setManualFocus(focus_manual);
            printDebugInfo = bundle.getBoolean(SHOW_DEBUGINFO, false);

            builder.setIsSlightBeauty(false);
        }

        //可以在这里做权限检查,若没有audio和camera权限,进一步引导用户做权限设置
        checkPermission();
        mStreamer = new KSYStreamer(this);
        mStreamer.setConfig(builder.build());
        mStreamer.setDisplayPreview(mCameraPreview);
        mStreamer.setOnStatusListener(mOnErrorListener);
        mStreamer.setOnLogListener(mOnLogListener);
        mStreamer.setOnAudioRawDataListener(mOnAudioRawDataListener);
        mStreamer.enableDebugLog(true);
        mStreamer.setMuteAudio(mute_audio);
        mStreamer.setEnableEarMirror(earMirror);
        mStreamer.setBeautyFilter(RecorderConstants.FILTER_BEAUTY_DENOISE);
        if (showWaterMark) {
            showWaterMark();
        }

        if (testSWFilterInterface) {
            mStreamer.setOnPreviewFrameListener(new OnPreviewFrameListener() {
                @Override
                public void onPreviewFrame(byte[] data, int width, int height, boolean isRecording) {
                    frameCount++;
                    if (isRecording) {
                        Arrays.fill(data, width * height, data.length, (byte) 128);
                    }
                    if (frameCount % 60 == 0) {
                        Log.e(TAG, "setOnPreviewFrameListener" + isRecording);
                    }
                }
            });
        }

        enable_beauty.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (encode_method == KSYStreamerConfig.ENCODE_METHOD.SOFTWARE) {
                        mStreamer.setBeautyFilter(RecorderConstants.FILTER_BEAUTY_DENOISE);
                    } else {
                        showChooseFilter();
                    }
                } else {
                    if (encode_method == KSYStreamerConfig.ENCODE_METHOD.SOFTWARE) {
                        mStreamer.setBeautyFilter(RecorderConstants.FILTER_BEAUTY_DISABLE);
                    } else {
                        mStreamer.setBeautyFilter(new KSYImageFilter());
                    }
                }
            }
        });

        mPip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long curTime = System.currentTimeMillis();
                if (curTime - lastPipClickTime < 1000) {
                    return;
                }
                lastPipClickTime = curTime;
                if (mPicPipMode) {
                    mStreamer.hidePipBitmap();
                    if (mPipBitmap != null) {
                        mPipBitmap.recycle();
                        mPipBitmap = null;
                    }
                    mPicPipMode = false;
                    mPicturePip.setText(CameraActivity.this.getResources().getString(R.string.picture_pip));
                    mPicturePip.postInvalidate();
                }

                if (!mPipMode) {
                    mKsyMediaPlayer = new KSYMediaPlayer.Builder(CameraActivity.this).build();
                    mKsyMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
                    mKsyMediaPlayer.setOnCompletionListener(mOnCompletionListener);
                    mKsyMediaPlayer.setOnInfoListener(mOnInfoListener);
                    mKsyMediaPlayer.setOnErrorListener(mOnPlayerErrorListener);
                    mKsyMediaPlayer.setOnSeekCompleteListener(mOnSeekCompletedListener);
                    mKsyMediaPlayer.setScreenOnWhilePlaying(true);
                    mKsyMediaPlayer.setBufferTimeMax(5);
                    mKsyMediaPlayer.setLooping(true);
                    mKsyMediaPlayer.setVolume(pipVolume, pipVolume);
                    mKsyMediaPlayer.setPlayerMute(earMirror ? 1 : 0);
                    mStreamer.setPipPlayer(mKsyMediaPlayer);
                    mStreamer.setHeadsetPlugged(true);
                    mStreamer.setPipLocation(0.6f, 0.6f, 0.4f, 0.4f);
                    mStreamer.startPlayer(mPipPath);
                    mPipMode = true;
                    mPip.setText(CameraActivity.this.getResources().getString(R.string.stop_pip));
                    mPip.postInvalidate();
                } else {
                    if (mKsyMediaPlayer != null) {
                        mKsyMediaPlayer.stop();
                        mKsyMediaPlayer.release();
                        mStreamer.stopPlayer();
                        mKsyMediaPlayer = null;
                    }
                    mPipMode = false;
                    mPip.setText(CameraActivity.this.getResources().getString(R.string.pip));
                    mPip.postInvalidate();
                }
            }
        });

        mPicturePip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long curTime = System.currentTimeMillis();
                if (curTime - lastPipClickTime < 1000) {
                    return;
                }
                lastPipClickTime = curTime;
                if (mPipMode) {
                    if (mKsyMediaPlayer != null) {
                        mKsyMediaPlayer.stop();
                        mKsyMediaPlayer.release();
                        mStreamer.stopPlayer();
                        mKsyMediaPlayer = null;
                    }
                    mPipMode = false;
                    mPip.setText(CameraActivity.this.getResources().getString(R.string.pip));
                    mPip.postInvalidate();
                }

                if (!mPicPipMode) {
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            mPipBitmap = BitmapFactory.decodeFile("/sdcard/test.png");
                            mStreamer.showPipBitmap(mPipBitmap, 0.6f, 0.6f, 0.4f, 0.4f);
                            mPicPipMode = true;
                            mPicturePip.setText(CameraActivity.this.getResources().getString(R.string.stop_picture_pip));
                            mPicturePip.postInvalidate();
                        }
                    });
                } else {
                    mStreamer.hidePipBitmap();
                    if (mPipBitmap != null) {
                        mPipBitmap.recycle();
                        mPipBitmap = null;
                    }
                    mPicPipMode = false;
                    mPicturePip.setText(CameraActivity.this.getResources().getString(R.string.picture_pip));
                    mPicturePip.postInvalidate();
                }
            }
        });


        mBgm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mKsyBgmPlayer = KSYBgmPlayer.getInstance();
                    mKsyBgmPlayer.setOnBgmPlayerListener(mBgmListener);
                    mKsyBgmPlayer.setVolume(bgmVolume);
                    mKsyBgmPlayer.setMute(earMirror);
                    mStreamer.setBgmPlayer(mKsyBgmPlayer);
                    mStreamer.startMixMusic(mBgmPath, true);
                    mStreamer.setHeadsetPlugged(true);
                } else {
                    mStreamer.stopMixMusic();
                }
            }
        });

        mEarMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                earMirror = isChecked;
                if (mKsyBgmPlayer != null) {
                    mKsyBgmPlayer.setMute(isChecked);
                }
                if (mKsyMediaPlayer != null) {
                    mKsyMediaPlayer.setPlayerMute(isChecked ? 1 : 0);
                }
                mStreamer.setEnableEarMirror(isChecked);
            }
        });

        mMuteAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mStreamer.setMuteAudio(isChecked);
            }
        });

        mWaterMark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    showWaterMark();
                else
                    hideWaterMark();
            }
        });

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
                        Log.e(TAG, "操作太频繁");
                    }
                } else {
                    checkPermission();
                    if (mStreamer.startStream()) {
                        mShootingText.setText(STOP_STRING);
                        mShootingText.postInvalidate();
                        recording = true;

                        mStreamer.setEnableReverb(true);
                        mStreamer.setReverbLevel(4);
                    } else {
                        Log.e(TAG, "操作太频繁");
                    }
                }

            }
        });
        if (startAuto) {
            mShootingText.setEnabled(false);
        }

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
        mDebugInfoTextView = (TextView) this.findViewById(R.id.debuginfo);
    }

    private void beginInfoUploadTimer() {
        if (printDebugInfo && timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
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
            }, 100, 3000);
        }
    }

    //update debug info
    private void updateDebugInfo() {
        if (mStreamer == null) return;
        mDebugInfo = String.format("RtmpHostIP()=%s DroppedFrameCount()=%d \n " +
                        "ConnectTime()=%d DnsParseTime()=%d \n " +
                        "UploadedKB()=%d EncodedFrames()=%d \n" +
                        "CurrentBitrate=%f Version()=%s",
                mStreamer.getRtmpHostIP(), mStreamer.getDroppedFrameCount(),
                mStreamer.getConnectTime(), mStreamer.getDnsParseTime(),
                mStreamer.getUploadedKBytes(), mStreamer.getEncodedFrames(),
                mStreamer.getCurrentBitrate(), mStreamer.getVersion());
    }

    //show watermark in specific location
    private void showWaterMark() {
        if (!landscape) {
            mStreamer.showWaterMarkLogo(mLogoPath, 0.08f, 0.06f, 0.27f, 0.15f, 0.8f);
            mStreamer.showWaterMarkTime(0.02f, 0.015f, 0.4f, Color.RED, 1.0f);
        } else {
            mStreamer.showWaterMarkLogo(mLogoPath, 0.06f, 0.08f, 0.15f, 0.27f, 0.8f);
            mStreamer.showWaterMarkTime(0.015f, 0.02f, 0.25f, Color.RED, 1.0f);
        }
    }

    private void hideWaterMark() {
        mStreamer.hideWaterMarkLogo();
        mStreamer.hideWaterMarkTime();
    }

    private void showChooseFilter() {
        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(this).setTitle("请选择美颜滤镜").setSingleChoiceItems(
                new String[]{"BEAUTY_SOFT", "SKIN_WHITEN", "BEAUTY_ILLUSION", "DENOISE","DEMOFILTER", "SPLIT_E/P_FILTER", "GROUP_FILTER"}, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which < 4) {
                            mStreamer.setBeautyFilter(which + 16);
                        } else if (which == 4) {

                        } else if (which == 5) {
                            mStreamer.setBeautyFilter(new DEMOFILTER(), RecorderConstants.FILTER_USAGE_ENCODE);
                            mStreamer.setBeautyFilter(new DEMOFILTER2(), RecorderConstants.FILTER_USAGE_PREVIEW);
                        } else if (which == 6) {
                            mStreamer.setBeautyFilter(new GroupFilterDemo());
                        }
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        //可以在这里做权限检查,若没有audio和camera权限,进一步引导用户做权限设置
        checkPermission();
        if (mKsyBgmPlayer != null) {
            mKsyBgmPlayer.resume();
        }
        if (mKsyMediaPlayer != null) {
            mKsyMediaPlayer.start();
        }
        mStreamer.onResume();
        mAcitivityResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mKsyBgmPlayer != null) {
            mKsyBgmPlayer.pause();
        }
        if (mKsyMediaPlayer != null) {
            mKsyMediaPlayer.pause();
        }
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
                                mStreamer.stopStream(true);
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


    public OnStatusListener mOnErrorListener = new OnStatusListener() {
        @Override
        public void onStatus(int what, int arg1, int arg2, String msg) {
            // msg may be null
            switch (what) {
                case RecorderConstants.KSYVIDEO_OPEN_STREAM_SUCC:
                    // 推流成功
                    Log.d("TAG", "KSYVIDEO_OPEN_STREAM_SUCC");
                    mHandler.obtainMessage(what, "start stream succ")
                            .sendToTarget();
                    break;
                case RecorderConstants.KSYVIDEO_ENCODED_FRAMES_FAILED:
                    //编码失败
                    Log.e(TAG, "---------KSYVIDEO_ENCODED_FRAMES_FAILED");
                    break;
                case RecorderConstants.KSYVIDEO_FRAME_DATA_SEND_SLOW:
                    //网络状况不佳
                    if (mHandler != null) {
                        mHandler.obtainMessage(what, "network not good").sendToTarget();
                    }
                    break;
                case RecorderConstants.KSYVIDEO_EST_BW_DROP:
                    //编码码率下降状态通知
                    break;
                case RecorderConstants.KSYVIDEO_EST_BW_RAISE:
                    //编码码率上升状态通知
                    break;
                case RecorderConstants.KSYVIDEO_AUDIO_INIT_FAILED:
                    //音频录制初始化失败回调
                    break;
                case RecorderConstants.KSYVIDEO_INIT_DONE:
                    mHandler.obtainMessage(what, "init done")
                            .sendToTarget();
                    break;
                case RecorderConstants.KSY_PIP_EXCEPTION:
                    mHandler.obtainMessage(what, "pip exception")
                            .sendToTarget();
                    break;
                case RecorderConstants.KSY_RENDER_EXCEPTION:
                    mHandler.obtainMessage(what, "renderer exception")
                            .sendToTarget();
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
                        if (!executorService.isShutdown()) {
                            executorService.submit(new Runnable() {

                                @Override
                                public void run() {
                                    boolean needReconnect = true;
                                    try {
                                        while (needReconnect) {
                                            Thread.sleep(3000);
                                            //只在Activity对用户可见时重连
                                            if (mAcitivityResumed) {
                                                if (mStreamer.startStream()) {
                                                    recording = true;
                                                    needReconnect = false;
                                                }
                                            }
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                            });
                        }
                    }
                    if (mHandler != null) {
                        mHandler.obtainMessage(what, msg).sendToTarget();
                    }
            }
        }

    };

    private KSYBgmPlayer.OnBgmPlayerListener mBgmListener = new KSYBgmPlayer.OnBgmPlayerListener() {
        @Override
        public void onCompleted() {
            Log.d(TAG, "End of the currently playing music");
        }

        @Override
        public void onError(int err) {
            Log.e(TAG, "onMusicError: " + err);
        }
    };

    private OnLogEventListener mOnLogListener = new OnLogEventListener() {
        @Override
        public void onLogEvent(StringBuffer singleLogContent) {
            Log.d(TAG, "***onLogEvent : " + singleLogContent.toString());
        }
    };

    private OnAudioRawDataListener mOnAudioRawDataListener = new OnAudioRawDataListener() {
        @Override
        public short[] OnAudioRawData(short[] data, int count) {
            //audio pcm data
            return data;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mKsyBgmPlayer != null) {
            mKsyBgmPlayer.release();
            mKsyBgmPlayer = null;
        }
        if (mKsyMediaPlayer != null) {
            mKsyMediaPlayer.release();
            mKsyMediaPlayer = null;
        }
        mStreamer.onDestroy();
        executorService.shutdownNow();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (timer != null) {
            timer.cancel();
        }
    }

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
                        mStreamer.stopStream(true);
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

    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            long duration = mKsyMediaPlayer.getDuration();
            long progress = duration * percent / 100;
        }
    };

    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompletedListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            Log.d(TAG, "onSeekComplete...............");
        }
    };

    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {

        }
    };

    private IMediaPlayer.OnErrorListener mOnPlayerErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            switch (what) {
                case KSYMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    Log.e(TAG, "OnErrorListener, Error Unknown:" + what + ",extra:" + extra);
                    break;
                default:
                    Log.e(TAG, "OnErrorListener, Error:" + what + ",extra:" + extra);
            }

            return false;
        }
    };

    public IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            Log.d(TAG, "onInfo, what:" + i + ",extra:" + i1);
            return false;
        }
    };

    private boolean checkPermission() {
        try {
            int pRecordAudio = PermissionChecker.checkCallingOrSelfPermission(this, "android.permission.RECORD_AUDIO");
            int pCamera = PermissionChecker.checkCallingOrSelfPermission(this, "android.permission.CAMERA");

            if(pRecordAudio != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG,"do not have AudioRecord permission, please check");
                Toast.makeText(this,"do not have AudioRecord permission, please check", Toast.LENGTH_LONG).show();
                return false;
            }
            if(pCamera != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG,"do not have CAMERA permission, please check");
                Toast.makeText(this,"do not have CAMERA permission, please check", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
