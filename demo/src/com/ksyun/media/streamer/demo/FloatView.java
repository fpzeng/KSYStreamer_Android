package com.ksyun.media.streamer.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * for float window move
 */

public class FloatView extends RelativeLayout {
    private static final String TAG = "FloatView";

    private float mTouchStartX;
    private float mTouchStartY;
    private int mOriginX;
    private int mOriginY;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWmParams;

    public FloatView(Context context) {
        super(context);
        init();
    }

    public FloatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setWmParams(WindowManager.LayoutParams params) {
        mWmParams = params;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //获取相对屏幕的坐标，即以屏幕左上角为原点
                mTouchStartX = event.getRawX();
                mTouchStartY = event.getRawY();
                //保存初始位置信息
                if (mWmParams != null) {
                    mOriginX = mWmParams.x;
                    mOriginY = mWmParams.y;
                }
                performClick();
                break;
            case MotionEvent.ACTION_MOVE:
                updateViewPosition(event.getRawX(), event.getRawY());
                break;
            case MotionEvent.ACTION_UP:
                mTouchStartX = 0;
                mTouchStartY = 0;
                break;
        }
        return true;
    }

    private void init() {
        mWindowManager = (WindowManager) getContext().getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
    }

    @SuppressLint("RtlHardcoded")
    private void updateViewPosition(float x, float y) {
        if (mWmParams != null) {
            // 计算偏移量
            float offX = x - mTouchStartX;
            float offY = y - mTouchStartY;
            if ((mWmParams.gravity & Gravity.RIGHT) != 0) {
                offX = -offX;
            }
            if ((mWmParams.gravity & Gravity.BOTTOM) != 0) {
                offY = -offY;
            }
            // 配置参数，刷新显示
            mWmParams.x = (int) (mOriginX + offX);
            mWmParams.y = (int) (mOriginY + offY);
            mWindowManager.updateViewLayout(this, mWmParams);
        }
    }
}
