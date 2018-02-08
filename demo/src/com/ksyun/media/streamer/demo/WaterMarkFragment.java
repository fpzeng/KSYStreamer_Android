package com.ksyun.media.streamer.demo;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.ksyun.media.streamer.capture.ImgTexSrcPin;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.util.BitmapLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.Unbinder;

/**
 * Watermark function switch.
 */

public class WaterMarkFragment extends Fragment {
    public static final String TAG = "WaterMarkFragment";
    protected static final int ANIMATED_WM_IDX = 3;
    protected static final int ADDITIONAL_WM_IDX = 4;

    @BindView(R.id.watermark)
    protected CheckBox mWatermarkCB;
    @BindView(R.id.animated_watermark)
    protected CheckBox mAnimatedWatermarkCB;
    @BindView(R.id.multi_watermark)
    protected CheckBox mMultiWatermarkCB;

    protected Unbinder mUnbinder;

    protected String mAnimatedLogoPath = "assets://ksyun.webp";
    protected StdCameraActivity mActivity;
    protected KSYStreamer mStreamer;
    protected AnimatedImageCapture mAnimatedImageCapture;
    protected ImgTexSrcPin mExtraWatermarkSrcPin;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.watermark_fragment, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mActivity = (StdCameraActivity) getActivity();
        mStreamer = mActivity.mStreamer;
        mAnimatedImageCapture = new AnimatedImageCapture(mStreamer.getGLRender());
        mExtraWatermarkSrcPin = new ImgTexSrcPin(mStreamer.getGLRender());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAnimatedImageCapture.release();
        mExtraWatermarkSrcPin.release();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 重新绘制水印，避免切后台时横竖屏切换的影响
        hideWatermark();
        showWatermark();
    }

    @OnCheckedChanged(R.id.watermark)
    protected void onWatermarkChecked(boolean checked) {
        if (checked) {
            showWatermark();
        } else {
            hideWatermark();
        }
        mAnimatedWatermarkCB.setEnabled(checked);
        mMultiWatermarkCB.setEnabled(checked);
    }

    @OnCheckedChanged(R.id.animated_watermark)
    protected void onAnimatedWatermarkChecked(boolean checked) {
        if (checked) {
            mStreamer.hideWaterMarkLogo();
            showAnimatedWatermark();
        } else {
            hideAnimatedWatermark();
            mActivity.showWaterMark();
        }
    }

    @OnCheckedChanged(R.id.multi_watermark)
    protected void onMultiWatermarkChecked(boolean checked) {
        if (checked) {
            showAdditionalWatermark();
        } else {
            hideAdditionalWatermark();
        }
    }

    protected void showWatermark() {
        mActivity.showWaterMark();
        if (mAnimatedWatermarkCB.isChecked()) {
            mStreamer.hideWaterMarkLogo();
            showAnimatedWatermark();
        }
        if (mMultiWatermarkCB.isChecked()) {
            showAdditionalWatermark();
        }
    }

    protected void hideWatermark() {
        hideAdditionalWatermark();
        hideAnimatedWatermark();
        mActivity.hideWaterMark();
    }

    protected void showAdditionalWatermark() {
        if (!mActivity.mIsLandscape) {
            showAdditionalWatermark(mActivity.mLogoPath, 0.70f, 0.04f, 0.20f, 0, 0.8f);
        } else {
            showAdditionalWatermark(mActivity.mLogoPath, 0.80f, 0.09f, 0, 0.20f, 0.8f);
        }
    }

    // show additional watermark logo, not valid in SOFTWARE_COMPAT mode
    protected void showAdditionalWatermark(String url, float x, float y,
                                           float w, float h, float a) {
        mExtraWatermarkSrcPin.connect(
                mStreamer.getImgTexPreviewMixer().getSinkPin(ADDITIONAL_WM_IDX)
        );
        mExtraWatermarkSrcPin.connect(
                mStreamer.getImgTexMixer().getSinkPin(ADDITIONAL_WM_IDX)
        );
        mStreamer.getImgTexPreviewMixer().setRenderRect(ADDITIONAL_WM_IDX, x, y, w, h, a);
        mStreamer.getImgTexMixer().setRenderRect(ADDITIONAL_WM_IDX, x, y, w, h, a);

        Bitmap bitmap = BitmapLoader.loadBitmap(getActivity(), url);
        if (bitmap != null) {
            mExtraWatermarkSrcPin.updateFrame(bitmap, true);
        }
    }

    protected void hideAdditionalWatermark() {
        mExtraWatermarkSrcPin.updateFrame(null, false);
        mExtraWatermarkSrcPin.disconnect(false);
    }

    protected void showAnimatedWatermark() {
        if (!mActivity.mIsLandscape) {
            showAnimatedWaterMark(mAnimatedLogoPath, 0.08f, 0.04f, 0.25f, 0, 0.8f);
        } else {
            showAnimatedWaterMark(mAnimatedLogoPath, 0.05f, 0.09f, 0, 0.25f, 0.8f);
        }
    }

    // show animated watermark logo, support gif/webp, not valid in SOFTWARE_COMPAT mode
    protected void showAnimatedWaterMark(String url, float x, float y, float w, float h, float a) {
        mAnimatedImageCapture.getSrcPin().connect(
                mStreamer.getImgTexPreviewMixer().getSinkPin(ANIMATED_WM_IDX));
        mAnimatedImageCapture.getSrcPin().connect(
                mStreamer.getImgTexMixer().getSinkPin(ANIMATED_WM_IDX));
        mStreamer.getImgTexPreviewMixer().setRenderRect(ANIMATED_WM_IDX, x, y, w, h, a);
        mStreamer.getImgTexMixer().setRenderRect(ANIMATED_WM_IDX, x, y, w, h, a);
        mAnimatedImageCapture.start(mActivity, url);
    }

    protected void hideAnimatedWatermark() {
        mAnimatedImageCapture.stop();
        mAnimatedImageCapture.getSrcPin().disconnect(false);
    }
}
