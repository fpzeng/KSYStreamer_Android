package com.ksyun.media.streamer.demo;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.ksyun.media.streamer.capture.ViewCapture;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.lht.paintview.PaintView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;

/**
 * Fragment for video settings.
 */

public class StreamFuncFragment extends Fragment {
    public static final String TAG = "StreamFuncFragment";
    protected static final int PAINT_VIEW_IDX = 7;

    @BindView(R.id.front_camera_mirror)
    protected CheckBox mFrontMirrorCB;
    @BindView(R.id.paint_streaming)
    protected CheckBox mPaintStreamingCB;

    protected StdCameraActivity mActivity;
    protected KSYStreamer mStreamer;
    protected ViewCapture mPaintViewCapture;
    protected PaintView mPaintView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stream_func_fragment, container, false);
        ButterKnife.bind(this, view);
        mActivity = (StdCameraActivity) getActivity();
        mStreamer = mActivity.mStreamer;
        mPaintView = mActivity.mPaintView;
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePaintViewCapture();
    }

    @OnCheckedChanged(R.id.front_camera_mirror)
    protected void onFrontMirrorChecked(boolean isChecked) {
        mStreamer.setFrontCameraMirror(isChecked);
    }

    @OnCheckedChanged(R.id.paint_streaming)
    protected void onPaintStreamingChecked(boolean isChecked) {
        if (isChecked) {
            // config paint view
            mPaintView.setVisibility(View.VISIBLE);
            mPaintView.setColor(Color.RED);
            mPaintView.setBgColor(Color.TRANSPARENT);
            mPaintView.setStrokeWidth(4);
            mPaintView.setGestureEnable(false);

            initPaintViewCapture();
            startPaintViewCapture();
        } else {
            stopPaintViewCapture();
            mPaintView.clear();
            mPaintView.setVisibility(View.GONE);
        }
    }

    protected void initPaintViewCapture() {
        if (mPaintViewCapture != null) {
            return;
        }

        mPaintViewCapture = new ViewCapture(mStreamer.getGLRender());
        // connect to the empty last sink pin of graph mixer
        mPaintViewCapture.getSrcPin().connect(
                mStreamer.getImgTexMixer().getSinkPin(PAINT_VIEW_IDX));
        // set render position relative to the video
        mStreamer.getImgTexMixer().setRenderRect(PAINT_VIEW_IDX, 0, 0, 1, 1, 1);

        // restart PaintViewCapture while view layout changed
        mPaintView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int oldW = oldRight - oldLeft;
                int oldH = oldBottom - oldTop;
                if (mPaintStreamingCB.isChecked() && (oldW * oldH != 0)) {
                    stopPaintViewCapture();
                    startPaintViewCapture();
                }
            }
        });
    }

    protected void startPaintViewCapture() {
        if (mPaintViewCapture != null) {
            mPaintViewCapture.setTargetResolution(mStreamer.getTargetWidth(),
                    mStreamer.getTargetHeight());
            mPaintViewCapture.setUpdateFps(mStreamer.getTargetFps());
            mPaintViewCapture.start(mPaintView);
        }
    }

    protected void stopPaintViewCapture() {
        if (mPaintViewCapture != null) {
            mPaintViewCapture.stop();
        }
    }

    protected void releasePaintViewCapture() {
        if (mPaintViewCapture != null) {
            mPaintViewCapture.release();
        }
    }
}
