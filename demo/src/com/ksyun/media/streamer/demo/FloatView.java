package com.ksyun.media.streamer.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * for float window move
 */

public class FloatView extends RelativeLayout {
    private float mTouchStartX;
    private float mTouchStartY;
    private float mLastX;
    private float mLastY;
    private float mMaxX = 0;
    private float mMaxY = 0;

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
    public boolean onTouchEvent(MotionEvent event) {
        //获取相对屏幕的坐标，即以屏幕左上角为原点
        mLastX = event.getRawX();
        mLastY = event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //获取相对View的坐标，即以此View左上角为原点
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                updateViewPosition();
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

    private void updateViewPosition() {
        if (mWmParams != null) {
            mMaxX = mWindowManager.getDefaultDisplay().getWidth() - this.getWidth();
            mMaxY = mWindowManager.getDefaultDisplay().getHeight() - this.getHeight();

            //更新浮动窗口位置参数
            float newX = (mLastX - mTouchStartX);
            float newY = (mLastY - mTouchStartY);
            //以屏幕左上角为原点
            mWmParams.gravity = Gravity.LEFT | Gravity.TOP;

            //不能移除屏幕最左边和最上边
            if (newX < 0) {
                newX = 0;
            }

            if (newY < 0) {
                newY = 0;
            }

            //不能移出屏幕最右边和最下边
            if (newX > mMaxX) {
                newX = mMaxX;
            }

            if (newY > mMaxY) {
                newY = mMaxY;
            }
            mWmParams.x = (int) newX;
            mWmParams.y = (int) newY;
            mWindowManager.updateViewLayout(this, mWmParams);  //刷新显示

        }
    }
}
