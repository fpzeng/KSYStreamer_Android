package com.ksyun.media.streamer.demo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.streamer.capture.CameraCapture;
import com.ksyun.media.streamer.capture.ViewCapture;
import com.ksyun.media.streamer.capture.camera.CameraTouchHelper;
import com.ksyun.media.streamer.filter.audio.AudioFilterBase;
import com.ksyun.media.streamer.filter.audio.AudioReverbFilter;
import com.ksyun.media.streamer.filter.audio.KSYAudioEffectFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyProFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySpecialEffectsFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyToneCurveFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterMgt;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.OnAudioRawDataListener;
import com.ksyun.media.streamer.kit.OnPreviewFrameListener;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.logstats.StatsLogReport;
import com.ksyun.media.streamer.util.gles.GLRender;
import com.lht.paintview.PaintView;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.ksyun.media.streamer.filter.audio.KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_FEMALE;
import static com.ksyun.media.streamer.filter.audio.KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_HEROIC;
import static com.ksyun.media.streamer.filter.audio.KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_MALE;
import static com.ksyun.media.streamer.filter.audio.KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_PITCH;
import static com.ksyun.media.streamer.filter.audio.KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_ROBOT;

public class CameraActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "CameraActivity";

    private GLSurfaceView mCameraPreviewView;
    //private TextureView mCameraPreviewView;
    private CameraHintView mCameraHintView;
    private PaintView mPaintView;
    private Chronometer mChronometer;
    private View mDeleteView;
    private View mSwitchCameraView;
    private View mFlashView;
    private View mAddView;
    private TextView mShootingText;
    private TextView mRecordingText;
    private TextView mCaptureSceenShot;
    private CheckBox mAudioLDCheckBox;
    private CheckBox mWaterMarkCheckBox;
    private CheckBox mBeautyCheckBox;
    private CheckBox mAudioPreviewCheckBox;
    private CheckBox mBgmCheckBox;
    private TextView mAudioFilterText;
    private TextView mBgmFilterText;
    private CheckBox mMuteCheckBox;
    private CheckBox mAudioOnlyCheckBox;
    private CheckBox mFrontMirrorCheckBox;
    private TextView mUrlTextView;
    private TextView mDebugInfoTextView;
    private CheckBox mPaintCheckBox;
    private CheckBox mBgImageCheckBox;

    private View mBeautyChooseView;
    private AppCompatSpinner mBeautySpinner;
    private LinearLayout mBeautyGrindLayout;
    private TextView mGrindText;
    private AppCompatSeekBar mGrindSeekBar;
    private LinearLayout mBeautyWhitenLayout;
    private TextView mWhitenText;
    private AppCompatSeekBar mWhitenSeekBar;
    private LinearLayout mBeautyRuddyLayout;
    private TextView mRuddyText;
    private AppCompatSeekBar mRuddySeekBar;

    private int mLastRotation;
    private OrientationEventListener mOrientationEventListener;

    private ButtonObserver mObserverButton;
    private CheckBoxObserver mCheckBoxObserver;

    private ViewCapture mPaintViewCapture;
    private KSYStreamer mStreamer;
    private Handler mMainHandler;
    private Timer mTimer;

    private boolean mAutoStart;
    private boolean mIsLandscape;
    private boolean mPrintDebugInfo = false;
    private boolean mRecording = false;
    private boolean mIsFileRecording = false;
    private boolean mIsFlashOpened = false;
    private String mUrl;
    private String mDebugInfo = "";
    private String mBgmPath = "/sdcard/test.mp3";
    private String mLogoPath = "file:///sdcard/test.png";
    private String mBgImagePath = "assets://bg.jpg";
    private String mRecordUrl = "/sdcard/rec_test.mp4";

    private boolean mHWEncoderUnsupported;
    private boolean mSWEncoderUnsupported;

    private final static int PERMISSION_REQUEST_CAMERA_AUDIOREC = 1;
    private static final String START_STRING = "开始直播";
    private static final String STOP_STRING = "停止直播";
    private static final String START_RECORDING = "开始录制";
    private static final String STOP_RECORDING = "停止录制";

    public final static String URL = "url";
    public final static String FRAME_RATE = "framerate";
    public final static String VIDEO_BITRATE = "video_bitrate";
    public final static String AUDIO_BITRATE = "audio_bitrate";
    public final static String CAP_RESOLUTION = "cap_resolution";
    public final static String PREVIEW_RESOLUTION = "preview_resolution";
    public final static String VIDEO_RESOLUTION = "video_resolution";
    public final static String ORIENTATION = "orientation";
    public final static String ENCODE_TYPE = "encode_type";
    public final static String ENCODE_METHOD = "encode_method";
    public final static String ENCODE_SCENE = "encode_scene";
    public final static String ENCODE_PROFILE = "encode_profile";
    public final static String STEREO_STREAM = "stereo_stream";
    public final static String START_AUTO = "start_auto";
    public static final String SHOW_DEBUGINFO = "show_debuginfo";

    public static void startActivity(Context context, int fromType,
                                     String rtmpUrl, int frameRate,
                                     int videoBitrate, int audioBitrate,
                                     int capResolution, int previewResolution,
                                     int targetResolution, int orientation,
                                     int encodeType, int encodeMethod,
                                     int encodeScene, int encodeProfile,
                                     boolean stereoStream,
                                     boolean startAuto, boolean showDebugInfo) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("type", fromType);
        intent.putExtra(URL, rtmpUrl);
        intent.putExtra(FRAME_RATE, frameRate);
        intent.putExtra(VIDEO_BITRATE, videoBitrate);
        intent.putExtra(AUDIO_BITRATE, audioBitrate);
        intent.putExtra(CAP_RESOLUTION, capResolution);
        intent.putExtra(PREVIEW_RESOLUTION, previewResolution);
        intent.putExtra(VIDEO_RESOLUTION, targetResolution);
        intent.putExtra(ORIENTATION, orientation);
        intent.putExtra(ENCODE_TYPE, encodeType);
        intent.putExtra(ENCODE_METHOD, encodeMethod);
        intent.putExtra(ENCODE_SCENE, encodeScene);
        intent.putExtra(ENCODE_PROFILE, encodeProfile);
        intent.putExtra(STEREO_STREAM, stereoStream);
        intent.putExtra(START_AUTO, startAuto);
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

        mCameraHintView = (CameraHintView) findViewById(R.id.camera_hint);
        mCameraPreviewView = (GLSurfaceView) findViewById(R.id.camera_preview);
        //mCameraPreviewView = (TextureView) findViewById(R.id.camera_preview);
        mUrlTextView = (TextView) findViewById(R.id.url);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mDebugInfoTextView = (TextView) findViewById(R.id.debuginfo);
        mPaintView = (PaintView) findViewById(R.id.view_paint);

        mObserverButton = new ButtonObserver();
        mShootingText = (TextView) findViewById(R.id.click_to_shoot);
        mShootingText.setOnClickListener(mObserverButton);
        mRecordingText = (TextView) findViewById(R.id.click_to_record);
        mRecordingText.setOnClickListener(mObserverButton);
        mCaptureSceenShot = (TextView) findViewById(R.id.click_to_capture_screenshot);
        mCaptureSceenShot.setOnClickListener(mObserverButton);
        mDeleteView = findViewById(R.id.backoff);
        mDeleteView.setOnClickListener(mObserverButton);
        mSwitchCameraView = findViewById(R.id.switch_cam);
        mSwitchCameraView.setOnClickListener(mObserverButton);
        mFlashView = findViewById(R.id.flash);
        mFlashView.setOnClickListener(mObserverButton);
        mAddView = findViewById(R.id.add);
        mAddView.setOnClickListener(mObserverButton);

        mCheckBoxObserver = new CheckBoxObserver();
        mBeautyCheckBox = (CheckBox) findViewById(R.id.click_to_switch_beauty);
        mBeautyCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mAudioFilterText = (TextView) findViewById(R.id.click_to_select_audio_filter);
        mAudioFilterText.setOnClickListener(mObserverButton);
        mBgmCheckBox = (CheckBox) findViewById(R.id.bgm);
        mBgmCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mBgmFilterText = (TextView) findViewById(R.id.bgm_filter);
        mBgmFilterText.setOnClickListener(mObserverButton);
        mAudioPreviewCheckBox = (CheckBox) findViewById(R.id.ear_mirror);
        mAudioPreviewCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mMuteCheckBox = (CheckBox) findViewById(R.id.mute);
        mMuteCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mWaterMarkCheckBox = (CheckBox) findViewById(R.id.watermark);
        mWaterMarkCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mFrontMirrorCheckBox = (CheckBox) findViewById(R.id.front_camera_mirror);
        mFrontMirrorCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mAudioOnlyCheckBox = (CheckBox) findViewById(R.id.audio_only);
        mAudioOnlyCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mAudioLDCheckBox = (CheckBox) findViewById(R.id.audio_ld);
        mAudioLDCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mPaintCheckBox = (CheckBox) findViewById(R.id.paint);
        mPaintCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mBgImageCheckBox = (CheckBox) findViewById(R.id.bg_image);
        mBgImageCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);

        mBeautyChooseView = findViewById(R.id.beauty_choose);
        mBeautySpinner = (AppCompatSpinner) findViewById(R.id.beauty_spin);
        mBeautyGrindLayout = (LinearLayout) findViewById(R.id.beauty_grind);
        mGrindText = (TextView) findViewById(R.id.grind_text);
        mGrindSeekBar = (AppCompatSeekBar) findViewById(R.id.grind_seek_bar);
        mBeautyWhitenLayout = (LinearLayout) findViewById(R.id.beauty_whiten);
        mWhitenText = (TextView) findViewById(R.id.whiten_text);
        mWhitenSeekBar = (AppCompatSeekBar) findViewById(R.id.whiten_seek_bar);
        mBeautyRuddyLayout = (LinearLayout) findViewById(R.id.beauty_ruddy);
        mRuddyText = (TextView) findViewById(R.id.ruddy_text);
        mRuddySeekBar = (AppCompatSeekBar) findViewById(R.id.ruddy_seek_bar);

        mMainHandler = new Handler();
        mStreamer = new KSYStreamer(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String url = bundle.getString(URL);
            if (!TextUtils.isEmpty(url)) {
                mUrl = url;
                mUrlTextView.setText(mUrl);
                mStreamer.setUrl(url);
            }

            int frameRate = bundle.getInt(FRAME_RATE, 0);
            if (frameRate > 0) {
                mStreamer.setPreviewFps(frameRate);
                mStreamer.setTargetFps(frameRate);
            }

            int videoBitrate = bundle.getInt(VIDEO_BITRATE, 0);
            if (videoBitrate > 0) {
                mStreamer.setVideoKBitrate(videoBitrate * 3 / 4, videoBitrate, videoBitrate / 4);
            }

            int audioBitrate = bundle.getInt(AUDIO_BITRATE, 0);
            if (audioBitrate > 0) {
                mStreamer.setAudioKBitrate(audioBitrate);
            }

            int capResolution = bundle.getInt(CAP_RESOLUTION, 0);
            mStreamer.setCameraCaptureResolution(capResolution);

            int previewResolution = bundle.getInt(PREVIEW_RESOLUTION, 0);
            mStreamer.setPreviewResolution(previewResolution);

            int videoResolution = bundle.getInt(VIDEO_RESOLUTION, 0);
            mStreamer.setTargetResolution(videoResolution);

            int encode_type = bundle.getInt(ENCODE_TYPE);
            mStreamer.setVideoCodecId(encode_type);

            int encode_method = bundle.getInt(ENCODE_METHOD);
            mStreamer.setEncodeMethod(encode_method);

            int encodeScene = bundle.getInt(ENCODE_SCENE);
            mStreamer.setVideoEncodeScene(encodeScene);

            int encodeProfile = bundle.getInt(ENCODE_PROFILE);
            mStreamer.setVideoEncodeProfile(encodeProfile);

            boolean stereoStream = bundle.getBoolean(STEREO_STREAM);
            mStreamer.setAudioChannels(stereoStream ? 2 : 1);

            int orientation = bundle.getInt(ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                int rotation = getDisplayRotation();
                mIsLandscape = (rotation % 180) != 0;
                mStreamer.setRotateDegrees(rotation);
                mLastRotation = rotation;
                mOrientationEventListener = new OrientationEventListener(this,
                        SensorManager.SENSOR_DELAY_NORMAL) {
                    @Override
                    public void onOrientationChanged(int orientation) {
                        int rotation = getDisplayRotation();
                        if (rotation != mLastRotation) {
                            Log.d(TAG, "Rotation changed " + mLastRotation + "->" + rotation);
                            mIsLandscape = (rotation % 180) != 0;
                            mStreamer.setRotateDegrees(rotation);
                            if (mWaterMarkCheckBox.isChecked()) {
                                hideWaterMark();
                                showWaterMark();
                            }
                            mLastRotation = rotation;
                        }
                    }
                };
            } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mIsLandscape = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mStreamer.setRotateDegrees(90);
            } else {
                mIsLandscape = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mStreamer.setRotateDegrees(0);
            }

            mAutoStart = bundle.getBoolean(START_AUTO, false);
            mPrintDebugInfo = bundle.getBoolean(SHOW_DEBUGINFO, false);
        }
        mStreamer.setOnLogEventListener(mOnLogEventListener);
        mStreamer.setDisplayPreview(mCameraPreviewView);
        mStreamer.setEnableRepeatLastFrame(false);  // disable repeat last frame in background
        mStreamer.setEnableAutoRestart(true, 3000); // enable auto restart
        mStreamer.setCameraFacing(CameraCapture.FACING_FRONT);
        mStreamer.setFrontCameraMirror(mFrontMirrorCheckBox.isChecked());
        mStreamer.setMuteAudio(mMuteCheckBox.isChecked());
        mStreamer.setEnableAudioPreview(mAudioPreviewCheckBox.isChecked());
        if(mStreamer.isAudioPreviewing() != mAudioPreviewCheckBox.isChecked()) {
            mAudioPreviewCheckBox.setChecked(mStreamer.isAudioPreviewing());
        }
        mStreamer.setOnInfoListener(mOnInfoListener);
        mStreamer.setOnErrorListener(mOnErrorListener);
        mStreamer.setOnLogEventListener(mOnLogEventListener);
        //mStreamer.setOnAudioRawDataListener(mOnAudioRawDataListener);
        //mStreamer.setOnPreviewFrameListener(mOnPreviewFrameListener);

        // set beauty filter
        initBeautyUI();
        if (mStreamer.getVideoEncodeMethod() == StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT) {
            mBeautyCheckBox.setChecked(true);
        }
        mStreamer.getImgTexFilterMgt().setOnErrorListener(new ImgTexFilterBase.OnErrorListener() {
            @Override
            public void onError(ImgTexFilterBase filter, int errno) {
                Toast.makeText(CameraActivity.this, "当前机型不支持该滤镜",
                        Toast.LENGTH_SHORT).show();
                mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(),
                        ImgTexFilterMgt.KSY_FILTER_BEAUTY_DISABLE);
            }
        });

        // add RGBA buffer filter to ImgTexFilterMgt, this would cause performance drop,
        // only valid after Android 4.4
        //RGBABufDemoFilter demoFilter = new RGBABufDemoFilter(mStreamer.getGLRender());
        //mStreamer.getImgTexFilterMgt().setExtraFilter(demoFilter);

        // touch focus and zoom support
        CameraTouchHelper cameraTouchHelper = new CameraTouchHelper();
        cameraTouchHelper.setCameraCapture(mStreamer.getCameraCapture());
        mCameraPreviewView.setOnTouchListener(cameraTouchHelper);
        // set CameraHintView to show focus rect and zoom ratio
        cameraTouchHelper.setCameraHintView(mCameraHintView);

        startCameraPreviewWithPermCheck(true);
        if (mWaterMarkCheckBox.isChecked()) {
            showWaterMark();
        }
    }

    private void initBeautyUI() {
        String[] items = new String[]{"DISABLE", "BEAUTY_SOFT", "SKIN_WHITEN", "BEAUTY_ILLUSION",
                "BEAUTY_DENOISE", "BEAUTY_SMOOTH", "BEAUTY_PRO", "BEAUTY_PRO2", "BEAUTY_PRO3",
                "BEAUTY_PRO4", "DEMO_FILTER", "GROUP_FILTER", "ToneCurve", "复古", "胶片"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBeautySpinner.setAdapter(adapter);
        mBeautySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = ((TextView) parent.getChildAt(0));
                if (textView != null) {
                    textView.setTextColor(getResources().getColor(R.color.font_color_35));
                }
                if (position == 0) {
                    mStreamer.getImgTexFilterMgt().setFilter((ImgFilterBase) null);
                } else if (position <= 5) {
                    mStreamer.getImgTexFilterMgt().setFilter(
                            mStreamer.getGLRender(), position + 15);
                } else if (position == 6) {
                    mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(),
                            ImgTexFilterMgt.KSY_FILTER_BEAUTY_PRO);
                } else if (position == 7) {
                    mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(),
                            ImgTexFilterMgt.KSY_FILTER_BEAUTY_PRO2);
                } else if (position == 8) {
                    mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(),
                            ImgTexFilterMgt.KSY_FILTER_BEAUTY_PRO3);
                } else if (position == 9) {
                    mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(),
                            ImgTexFilterMgt.KSY_FILTER_BEAUTY_PRO4);
                } else if (position == 10) {
                    mStreamer.getImgTexFilterMgt().setFilter(
                            new DemoFilter(mStreamer.getGLRender()));
                } else if (position == 11) {
                    List<ImgFilterBase> groupFilter = new LinkedList<>();
                    groupFilter.add(new DemoFilter2(mStreamer.getGLRender()));
                    groupFilter.add(new DemoFilter3(mStreamer.getGLRender()));
                    groupFilter.add(new DemoFilter4(mStreamer.getGLRender()));
                    groupFilter.add(new ImgBeautySpecialEffectsFilter(mStreamer.getGLRender(),
                            CameraActivity.this,
                            ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_BLUE));
                    mStreamer.getImgTexFilterMgt().setFilter(groupFilter);
                } else if (position == 12) {
                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mStreamer.getGLRender());
                    acvFilter.setFromCurveFileInputStream(
                            CameraActivity.this.getResources().openRawResource(R.raw.tone_cuver_sample));

                    mStreamer.getImgTexFilterMgt().setFilter(acvFilter);
                } else if (position == 13) {
                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mStreamer.getGLRender());
                    acvFilter.setFromCurveFileInputStream(
                            CameraActivity.this.getResources().openRawResource(R.raw.fugu));

                    mStreamer.getImgTexFilterMgt().setFilter(acvFilter);
                } else if (position == 14) {
                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mStreamer.getGLRender());
                    acvFilter.setFromCurveFileInputStream(
                            CameraActivity.this.getResources().openRawResource(R.raw.jiaopian));

                    mStreamer.getImgTexFilterMgt().setFilter(acvFilter);
                }
                List<ImgFilterBase> filters = mStreamer.getImgTexFilterMgt().getFilter();
                if (filters != null && !filters.isEmpty()) {
                    final ImgFilterBase filter = filters.get(0);
                    mBeautyGrindLayout.setVisibility(filter.isGrindRatioSupported() ?
                            View.VISIBLE : View.GONE);
                    mBeautyWhitenLayout.setVisibility(filter.isWhitenRatioSupported() ?
                            View.VISIBLE : View.GONE);
                    mBeautyRuddyLayout.setVisibility(filter.isRuddyRatioSupported() ?
                            View.VISIBLE : View.GONE);
                    SeekBar.OnSeekBarChangeListener seekBarChangeListener =
                            new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress,
                                                              boolean fromUser) {
                                    if (!fromUser) {
                                        return;
                                    }
                                    float val = progress / 100.f;
                                    if (seekBar == mGrindSeekBar) {
                                        filter.setGrindRatio(val);
                                    } else if (seekBar == mWhitenSeekBar) {
                                        filter.setWhitenRatio(val);
                                    } else if (seekBar == mRuddySeekBar) {
                                        if (filter instanceof ImgBeautyProFilter) {
                                            val = progress / 50.f - 1.0f;
                                        }
                                        filter.setRuddyRatio(val);
                                    }
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            };
                    mGrindSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
                    mWhitenSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
                    mRuddySeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
                    mGrindSeekBar.setProgress((int)(filter.getGrindRatio() * 100));
                    mWhitenSeekBar.setProgress((int)(filter.getWhitenRatio() * 100));
                    int ruddyVal = (int)(filter.getRuddyRatio() * 100);
                    if (filter instanceof ImgBeautyProFilter) {
                        ruddyVal = (int)(filter.getRuddyRatio() * 50 + 50);
                    }
                    mRuddySeekBar.setProgress(ruddyVal);
                } else {
                    mBeautyGrindLayout.setVisibility(View.GONE);
                    mBeautyWhitenLayout.setVisibility(View.GONE);
                    mBeautyRuddyLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
        mBeautySpinner.setPopupBackgroundResource(R.color.transparent1);
        mBeautySpinner.setSelection(4);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOrientationEventListener != null &&
                mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }

        mStreamer.setDisplayPreview(mCameraPreviewView);
        mStreamer.onResume();
        mCameraHintView.hideAll();

        if (!mBgImageCheckBox.isChecked()) {
            // camera may be occupied by other app in background
            startCameraPreviewWithPermCheck(false);
        }

        // re-enable audio low delay in foreground
        if (mAudioLDCheckBox.isChecked()) {
            mStreamer.setEnableAudioLowDelay(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
        mStreamer.onPause();

        // disable audio low delay in background
        if (mAudioLDCheckBox.isChecked()) {
            mStreamer.setEnableAudioLowDelay(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // stop paint view capture if needed
        stopPaintViewCapture();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        mStreamer.setOnLogEventListener(null);
        mStreamer.release();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackoffClick();
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private int getDisplayRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    //start streaming
    private void startStream() {
        mStreamer.startStream();
        mShootingText.setText(STOP_STRING);
        mShootingText.postInvalidate();
        mRecording = true;
    }

    //start recording to a local file
    private void startRecord() {
        mStreamer.startRecord(mRecordUrl);
        mRecordingText.setText(STOP_RECORDING);
        mRecordingText.postInvalidate();
        mIsFileRecording = true;
    }

    private void stopRecord() {
        mStreamer.stopRecord();
        mRecordingText.setText(START_RECORDING);
        mRecordingText.postInvalidate();
        mIsFileRecording = false;
        stopChronometer();
    }

    private void stopChronometer() {
        if (mRecording || mIsFileRecording) {
            return;
        }
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
    }

    private void stopStream() {
        // stop stream
        mStreamer.stopStream();
        mShootingText.setText(START_STRING);
        mShootingText.postInvalidate();
        mRecording = false;
        stopChronometer();
    }

    private void beginInfoUploadTimer() {
        if (mPrintDebugInfo && mTimer == null) {
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
            }, 100, 1000);
        }
    }

    //update debug info
    private void updateDebugInfo() {
        if (mStreamer == null) return;
        mDebugInfo = String.format(Locale.getDefault(),
                "RtmpHostIP()=%s DroppedFrameCount()=%d \n " +
                        "ConnectTime()=%d DnsParseTime()=%d \n " +
                        "UploadedKB()=%d EncodedFrames()=%d \n" +
                        "CurrentKBitrate=%d Version()=%s",
                mStreamer.getRtmpHostIP(), mStreamer.getDroppedFrameCount(),
                mStreamer.getConnectTime(), mStreamer.getDnsParseTime(),
                mStreamer.getUploadedKBytes(), mStreamer.getEncodedFrames(),
                mStreamer.getCurrentUploadKBitrate(), KSYStreamer.getVersion());
    }

    //show watermark in specific location
    private void showWaterMark() {
        if (!mIsLandscape) {
            mStreamer.showWaterMarkLogo(mLogoPath, 0.08f, 0.04f, 0.20f, 0, 0.8f);
            mStreamer.showWaterMarkTime(0.03f, 0.01f, 0.35f, Color.WHITE, 1.0f);
        } else {
            mStreamer.showWaterMarkLogo(mLogoPath, 0.05f, 0.09f, 0, 0.20f, 0.8f);
            mStreamer.showWaterMarkTime(0.01f, 0.03f, 0.22f, Color.WHITE, 1.0f);
        }
    }

    private void hideWaterMark() {
        mStreamer.hideWaterMarkLogo();
        mStreamer.hideWaterMarkTime();
    }

    // Example to handle camera related operation
    private void setCameraAntiBanding50Hz() {
        Camera.Parameters parameters = mStreamer.getCameraCapture().getCameraParameters();
        if (parameters != null) {
            parameters.setAntibanding(Camera.Parameters.ANTIBANDING_50HZ);
            mStreamer.getCameraCapture().setCameraParameters(parameters);
        }
    }

    private KSYStreamer.OnInfoListener mOnInfoListener = new KSYStreamer.OnInfoListener() {
        @Override
        public void onInfo(int what, int msg1, int msg2) {
            switch (what) {
                case StreamerConstants.KSY_STREAMER_CAMERA_INIT_DONE:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_INIT_DONE");
                    setCameraAntiBanding50Hz();
                    break;
                case StreamerConstants.KSY_STREAMER_OPEN_STREAM_SUCCESS:
                    Log.d(TAG, "KSY_STREAMER_OPEN_STREAM_SUCCESS");
                    mShootingText.setText(STOP_STRING);
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    beginInfoUploadTimer();
                    break;
                case StreamerConstants.KSY_STREAMER_OPEN_FILE_SUCCESS:
                    Log.d(TAG, "KSY_STREAMER_OPEN_FILE_SUCCESS");
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    break;
                case StreamerConstants.KSY_STREAMER_FRAME_SEND_SLOW:
                    Log.d(TAG, "KSY_STREAMER_FRAME_SEND_SLOW " + msg1 + "ms");
                    Toast.makeText(CameraActivity.this, "Network not good!",
                            Toast.LENGTH_SHORT).show();
                    break;
                case StreamerConstants.KSY_STREAMER_EST_BW_RAISE:
                    Log.d(TAG, "BW raise to " + msg1 / 1000 + "kbps");
                    break;
                case StreamerConstants.KSY_STREAMER_EST_BW_DROP:
                    Log.d(TAG, "BW drop to " + msg1 / 1000 + "kpbs");
                    break;
                default:
                    Log.d(TAG, "OnInfo: " + what + " msg1: " + msg1 + " msg2: " + msg2);
                    break;
            }
        }
    };

    private void handleEncodeError() {
        int encodeMethod = mStreamer.getVideoEncodeMethod();
        if (encodeMethod == StreamerConstants.ENCODE_METHOD_HARDWARE) {
            mHWEncoderUnsupported = true;
            if (mSWEncoderUnsupported) {
                mStreamer.setEncodeMethod(
                        StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT);
                Log.e(TAG, "Got HW encoder error, switch to SOFTWARE_COMPAT mode");
            } else {
                mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);
                Log.e(TAG, "Got HW encoder error, switch to SOFTWARE mode");
            }
        } else if (encodeMethod == StreamerConstants.ENCODE_METHOD_SOFTWARE) {
            mSWEncoderUnsupported = true;
            if (mHWEncoderUnsupported) {
                mStreamer.setEncodeMethod(
                        StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT);
                onBeautyChecked(mBeautyCheckBox.isChecked());
                Log.e(TAG, "Got SW encoder error, switch to SOFTWARE_COMPAT mode");
            } else {
                mStreamer.setEncodeMethod(StreamerConstants.ENCODE_METHOD_HARDWARE);
                Log.e(TAG, "Got SW encoder error, switch to HARDWARE mode");
            }
        }
    }

    private KSYStreamer.OnErrorListener mOnErrorListener = new KSYStreamer.OnErrorListener() {
        @Override
        public void onError(int what, int msg1, int msg2) {
            switch (what) {
                case StreamerConstants.KSY_STREAMER_ERROR_DNS_PARSE_FAILED:
                    Log.d(TAG, "KSY_STREAMER_ERROR_DNS_PARSE_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_FAILED:
                    Log.d(TAG, "KSY_STREAMER_ERROR_CONNECT_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_PUBLISH_FAILED:
                    Log.d(TAG, "KSY_STREAMER_ERROR_PUBLISH_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_BREAKED:
                    Log.d(TAG, "KSY_STREAMER_ERROR_CONNECT_BREAKED");
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_AV_ASYNC:
                    Log.d(TAG, "KSY_STREAMER_ERROR_AV_ASYNC " + msg1 + "ms");
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                    Log.d(TAG, "KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED");
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_ERROR_START_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_ERROR_SERVER_DIED");
                    break;
                //Camera was disconnected due to use by higher priority user.
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_EVICTED:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_ERROR_EVICTED");
                    break;
                default:
                    Log.d(TAG, "what=" + what + " msg1=" + msg1 + " msg2=" + msg2);
                    break;
            }
            switch (what) {
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_EVICTED:
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                    mStreamer.stopCameraPreview();
                    break;
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_CLOSE_FAILED:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_ERROR_UNKNOWN:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_OPEN_FAILED:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_FORMAT_NOT_SUPPORTED:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_WRITE_FAILED:
                    stopRecord();
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                {
                    handleEncodeError();
                    stopStream();
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startStream();
                        }
                    }, 3000);
                }
                break;
                default:
                    if(mStreamer.getEnableAutoRestart()) {
                        mShootingText.setText(START_STRING);
                        mShootingText.postInvalidate();
                        mRecording = false;
                        stopChronometer();
                    } else {
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
    };

    private StatsLogReport.OnLogEventListener mOnLogEventListener =
            new StatsLogReport.OnLogEventListener() {
                @Override
                public void onLogEvent(StringBuilder singleLogContent) {
                    Log.i(TAG, "***onLogEvent : " + singleLogContent.toString());
                }
            };

    private OnAudioRawDataListener mOnAudioRawDataListener = new OnAudioRawDataListener() {
        @Override
        public short[] OnAudioRawData(short[] data, int count, int sampleRate, int channels) {
            Log.d(TAG, "OnAudioRawData data.length=" + data.length + " count=" + count +
                    " sampleRate=" + sampleRate + " channels=" + channels);
            //audio pcm data
            return data;
        }
    };

    private OnPreviewFrameListener mOnPreviewFrameListener = new OnPreviewFrameListener() {
        @Override
        public void onPreviewFrame(byte[] data, int width, int height, boolean isRecording) {
            Log.d(TAG, "onPreviewFrame data.length=" + data.length + " " +
                    width + "x" + height + " mRecording=" + isRecording);
        }
    };

    private void onSwitchCamera() {
        mStreamer.switchCamera();
        mCameraHintView.hideAll();
    }

    private void onFlashClick() {
        if (mIsFlashOpened) {
            mStreamer.toggleTorch(false);
            mIsFlashOpened = false;
        } else {
            mStreamer.toggleTorch(true);
            mIsFlashOpened = true;
        }
    }

    private void onAddClick() {
        Intent intent = new Intent(getApplicationContext(), FloatViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        KSYGlobalStreamer.setInstance(mStreamer);
        startActivity(intent);
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
                        mChronometer.stop();
                        CameraActivity.this.finish();
                    }
                }).show();
    }

    private void onShootClick() {
        if (mRecording) {
            stopStream();
        } else {
            startStream();
        }
    }

    private void onRecordClick() {
        if (mIsFileRecording) {
            stopRecord();
        } else {
            startRecord();
        }
    }

    private boolean[] mChooseFilter = {false, false, false, false
            ,false ,false, false, false, false, false};

    private void showChooseAudioFilter() {
        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("请选择音频滤镜")
                .setMultiChoiceItems(
                        new String[]{"REVERB", "DEMO", "萝莉",
                                "大叔","庄严","机器人"}, mChooseFilter,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    mChooseFilter[which] = true;
                                }
                            }
                        }
                ).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<AudioFilterBase> filters = new LinkedList<>();
                        if (mChooseFilter[0]) {
                            AudioReverbFilter reverbFilter = new AudioReverbFilter();
                            filters.add(reverbFilter);
                        }
                        if (mChooseFilter[1]) {
                            DemoAudioFilter demofilter = new DemoAudioFilter();
                            filters.add(demofilter);
                        }
                        if (mChooseFilter[2]) {
                            KSYAudioEffectFilter audioEffect = new KSYAudioEffectFilter(AUDIO_EFFECT_TYPE_FEMALE);
                            filters.add(audioEffect);
                        }
                        if (mChooseFilter[3]) {
                            KSYAudioEffectFilter audioEffect = new KSYAudioEffectFilter(AUDIO_EFFECT_TYPE_MALE);
                            filters.add(audioEffect);
                        }
                        if (mChooseFilter[4]) {
                            KSYAudioEffectFilter audioEffect = new KSYAudioEffectFilter(AUDIO_EFFECT_TYPE_HEROIC);
                            filters.add(audioEffect);
                        }
                        if (mChooseFilter[5]) {
                            KSYAudioEffectFilter audioEffect = new KSYAudioEffectFilter(AUDIO_EFFECT_TYPE_ROBOT);
                            filters.add(audioEffect);
                        }

                        if (!mChooseFilter[0] && !mChooseFilter[1] && !mChooseFilter[2] &&
                                !mChooseFilter[3] && !mChooseFilter[4] && !mChooseFilter[5]) {
                            filters = null;
                        }
                        mStreamer.getAudioFilterMgt().setFilter(filters);

                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private int mChooseBGMFilter = 3;
    private String[] mBGMFilterName = {"-3", "-2", "-1", "0", "1", "2", "3"};

    private void onChooseBGMFilter() {
        AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("请选择背景音乐变调等级")
                .setSingleChoiceItems(mBGMFilterName, mChooseBGMFilter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChooseBGMFilter = which;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int level = Integer.parseInt(mBGMFilterName[mChooseBGMFilter]);
                        KSYAudioEffectFilter audioEffect = new KSYAudioEffectFilter(AUDIO_EFFECT_TYPE_PITCH);
                        audioEffect.setPitchLevel(level);
                        mStreamer.getBGMAudioFilterMgt().setFilter(audioEffect);
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void onBeautyChecked(boolean isChecked) {
        if (mStreamer.getVideoEncodeMethod() == StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT) {
            mStreamer.getImgTexFilterMgt().setFilter(mStreamer.getGLRender(), isChecked ?
                    ImgTexFilterMgt.KSY_FILTER_BEAUTY_DENOISE :
                    ImgTexFilterMgt.KSY_FILTER_BEAUTY_DISABLE);
            mStreamer.setEnableImgBufBeauty(isChecked);
        } else {
            mBeautyChooseView.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void onBgmChecked(boolean isChecked) {
        if (isChecked) {
            mStreamer.getAudioPlayerCapture().setOnCompletionListener(
                    new IMediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(IMediaPlayer iMediaPlayer) {
                            Log.d(TAG, "End of the currently playing music");
                        }
                    });
            mStreamer.getAudioPlayerCapture().setOnErrorListener(
                    new IMediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
                            Log.e(TAG, "OnErrorListener, Error:" + what + ", extra:" + extra);
                            return false;
                        }
                    });
            mStreamer.getAudioPlayerCapture().setVolume(0.4f);
            mStreamer.setEnableAudioMix(true);
            mStreamer.startBgm(mBgmPath, true);
        } else {
            mStreamer.stopBgm();
        }
    }

    private void onAudioPreviewChecked(boolean isChecked) {
        mStreamer.setEnableAudioPreview(isChecked);
    }

    private void onMuteChecked(boolean isChecked) {
        mStreamer.setMuteAudio(isChecked);
    }

    private void onWaterMarkChecked(boolean isChecked) {
        if (isChecked)
            showWaterMark();
        else
            hideWaterMark();
    }

    private void onFrontMirrorChecked(boolean isChecked) {
        mStreamer.setFrontCameraMirror(isChecked);
    }

    private void onAudioOnlyChecked(boolean isChecked) {
        mStreamer.setAudioOnly(isChecked);
    }

    private void onAudioLDChecked(boolean isChecked) {
        mStreamer.setEnableAudioLowDelay(isChecked);
    }

    private void onPaintChecked(boolean isChecked) {
        if (isChecked) {
            // config paint view
            mPaintView.setVisibility(View.VISIBLE);
            mPaintView.setColor(Color.RED);
            mPaintView.setBgColor(Color.TRANSPARENT);
            mPaintView.setStrokeWidth(4);
            mPaintView.setGestureEnable(false);

            if (mPaintViewCapture == null) {
                mPaintViewCapture = new ViewCapture(mStreamer.getGLRender());
                // connect to the empty last sink pin of graph mixer
                mPaintViewCapture.getSrcPin().connect(mStreamer.getImgTexMixer().getSinkPin(7));
                // set render position relative to the video
                mStreamer.getImgTexMixer().setRenderRect(7, 0, 0, 1, 1, 1);

                // restart PaintViewCapture while view layout changed
                mPaintView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        int oldW = oldRight - oldLeft;
                        int oldH = oldBottom - oldTop;
                        if (mPaintCheckBox.isChecked() && (oldW * oldH != 0)) {
                            stopPaintViewCapture();
                            startPaintViewCapture();
                        }
                    }
                });
            }
            startPaintViewCapture();
        } else {
            stopPaintViewCapture();
            mPaintView.clear();
            mPaintView.setVisibility(View.GONE);
        }
    }

    private void onBgImageChecked(boolean isChecked) {
        if (isChecked) {
            mStreamer.stopCameraPreview();
            mStreamer.startImageCapture(mBgImagePath);
        } else {
            mStreamer.stopImageCapture();
            mStreamer.startCameraPreview();
        }
    }

    private void startPaintViewCapture() {
        if (mPaintViewCapture != null) {
            mPaintViewCapture.setTargetResolution(mStreamer.getTargetWidth(),
                    mStreamer.getTargetHeight());
            mPaintViewCapture.setUpdateFps(mStreamer.getTargetFps());
            mPaintViewCapture.start(mPaintView);
        }
    }

    private void stopPaintViewCapture() {
        if (mPaintViewCapture != null) {
            mPaintViewCapture.stop();
        }
    }

    private void onCaptureScreenShotClick() {
        mStreamer.requestScreenShot(new GLRender.ScreenShotListener() {
            @Override
            public void onBitmapAvailable(Bitmap bitmap) {
                BufferedOutputStream bos = null;
                try {
                    Date date = new Date() ;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss") ;
                    final String filename = "/sdcard/screenshot"+ dateFormat.format(date) + ".jpg";

                    bos = new BufferedOutputStream(new FileOutputStream(filename));
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(CameraActivity.this, "保存截图到 "+ filename,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (bos != null) try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.switch_cam:
                    onSwitchCamera();
                    break;
                case R.id.backoff:
                    onBackoffClick();
                    break;
                case R.id.flash:
                    onFlashClick();
                    break;
                case R.id.add:
                    onAddClick();
                    break;
                case R.id.click_to_shoot:
                    onShootClick();
                    break;
                case R.id.click_to_record:
                    onRecordClick();
                    break;
                case R.id.click_to_capture_screenshot:
                    onCaptureScreenShotClick();
                    break;
                case R.id.bgm_filter:
                    onChooseBGMFilter();
                    break;
                case R.id.click_to_select_audio_filter:
                    showChooseAudioFilter();
                    break;
                default:
                    break;
            }
        }
    }

    private class CheckBoxObserver implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.click_to_switch_beauty:
                    onBeautyChecked(isChecked);
                    break;
                case R.id.bgm:
                    onBgmChecked(isChecked);
                    break;
                case R.id.ear_mirror:
                    onAudioPreviewChecked(isChecked);
                    break;
                case R.id.mute:
                    onMuteChecked(isChecked);
                    break;
                case R.id.watermark:
                    onWaterMarkChecked(isChecked);
                    break;
                case R.id.front_camera_mirror:
                    onFrontMirrorChecked(isChecked);
                    break;
                case R.id.audio_only:
                    onAudioOnlyChecked(isChecked);
                    break;
                case R.id.audio_ld:
                    onAudioLDChecked(isChecked);
                    break;
                case R.id.paint:
                    onPaintChecked(isChecked);
                    break;
                case R.id.bg_image:
                    onBgImageChecked(isChecked);
                    break;
                default:
                    break;
            }
        }
    }

    private void startCameraPreviewWithPermCheck(boolean request) {
        int cameraPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int audioPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (cameraPerm != PackageManager.PERMISSION_GRANTED ||
                audioPerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !request) {
                Log.e(TAG, "No CAMERA or AudioRecord permission, please check");
                Toast.makeText(this, "No CAMERA or AudioRecord permission, please check",
                        Toast.LENGTH_LONG).show();
            } else {
                String[] permissions = {Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSION_REQUEST_CAMERA_AUDIOREC);
            }
        } else {
            mStreamer.startCameraPreview();
            if (mAutoStart) {
                mAutoStart = false;
                startStream();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA_AUDIOREC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mStreamer.startCameraPreview();
                    if (mAutoStart) {
                        mAutoStart = false;
                        startStream();
                    }
                } else {
                    Log.e(TAG, "No CAMERA or AudioRecord permission");
                    Toast.makeText(this, "No CAMERA or AudioRecord permission",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}