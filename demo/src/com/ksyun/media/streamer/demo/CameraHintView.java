package com.ksyun.media.streamer.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.ksyun.media.streamer.capture.camera.ICameraHintView;

import java.util.Locale;

/**
 * View to show camera focus rect and zoom value.
 */

public class CameraHintView extends View implements ICameraHintView {
    private static final String TAG = "CameraHintView";
    private static final int COLOR_FOCUSING = 0xeed7d7d7;
    private static final int COLOR_FOCUSED = 0xee00ff00;
    private static final int COLOR_UNFOCUSED = 0xeeff0000;
    private static final int COLOR_ZOOM_TEXT = 0xffffffff;

    private boolean mShowRect;
    private Rect mRect;
    private Paint mFocusPaint;
    private Handler mHandler;
    private Runnable mHideRect;

    private Paint mZoomPaint;
    private boolean mShowZoomRatio;
    private float mZoomRatio;
    private Runnable mHideZoom;

    public CameraHintView(Context context) {
        super(context);
        init();
    }

    public CameraHintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mHandler = new Handler();
        mHideRect = new Runnable() {
            @Override
            public void run() {
                mShowRect = false;
                invalidate();
            }
        };

        mHideZoom = new Runnable() {
            @Override
            public void run() {
                mShowZoomRatio = false;
                invalidate();
            }
        };

        mFocusPaint = new Paint();
        mFocusPaint.setStyle(Paint.Style.STROKE);
        mFocusPaint.setStrokeWidth(dpToPx(1));

        mZoomPaint = new Paint();
        mZoomPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        mZoomPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mZoomPaint.setStrokeWidth(1);
        mZoomPaint.setColor(COLOR_ZOOM_TEXT);
        mZoomPaint.setTextAlign(Paint.Align.CENTER);
        mZoomPaint.setTextSize(dpToPx(16));
        mZoomPaint.setAntiAlias(true);
        mZoomPaint.setFilterBitmap(true);
    }

    public void hideAll() {
        mHandler.removeCallbacks(mHideRect);
        mHandler.removeCallbacks(mHideZoom);
        mHandler.post(mHideRect);
        mHandler.post(mHideZoom);
    }

    @Override
    public void updateZoomRatio(float val) {
        mShowZoomRatio = true;
        mZoomRatio = val;
        mHandler.removeCallbacks(mHideZoom);
        if (val == 1.0f) {
            mHandler.postDelayed(mHideZoom, 1000);
        }
        invalidate();
    }

    @Override
    public void startFocus(Rect rect) {
        mShowRect = true;
        mRect = rect;
        mFocusPaint.setColor(COLOR_FOCUSING);
        mHandler.removeCallbacks(mHideRect);
        mHandler.postDelayed(mHideRect, 3000);
        invalidate();
    }

    @Override
    public void setFocused(boolean success) {
        mShowRect = true;
        mFocusPaint.setColor(success ? COLOR_FOCUSED : COLOR_UNFOCUSED);
        mHandler.removeCallbacks(mHideRect);
        mHandler.postDelayed(mHideRect, 400);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mShowRect) {
            mFocusPaint.setStrokeWidth(3);
            mFocusPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(mRect.left, mRect.top, mRect.right, mRect.bottom, mFocusPaint);
        }
        if (mShowZoomRatio) {
            String text = String.format(Locale.getDefault(), "%.1f", mZoomRatio) + "x";
            int x = (int) (getWidth() * 0.5f);
            int y = (int) dpToPx(48);
            canvas.drawText(text, x, y, mZoomPaint);
        }
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
