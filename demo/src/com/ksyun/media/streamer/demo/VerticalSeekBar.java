package com.ksyun.media.streamer.demo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.util.Log;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

/**
 * 自定义竖直SeekBar.
 */

public class VerticalSeekBar extends FrameLayout{
    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initialize(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initialize(context);
    }

    public VerticalSeekBar(Context context)
    {
        super(context);
        initialize(context);
    }

    private View mProgressBg;
    private View mSecondaryProgressBg;
    private View mBg;
    private FrameLayout mSlider;
    private int mProgress = 50;
    private int mSecondaryProgress = 50;
    private int mMax = 100;
    private boolean mTrackingTouch = false;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private int mBGSize =6;          //进度条大小
    private int mBGRadius=20;     //进度条圆角
    private int mCircleSize=50;       //圆的大小
    private TextView mTextView;   //字


    public interface OnSeekBarChangeListener
    {
        void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser);
        void onStartTrackingTouch(VerticalSeekBar seekBar);
        void onStopTrackingTouch(VerticalSeekBar seekBar);
        void onRequestDisallowInterceptTouchEvent(boolean enable);
    }

    private void initialize(Context context)
    {
        LayoutParams params = new LayoutParams(mBGSize, LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        mBg = new View(context);
        addView(mBg, params);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(0xffbcbab8);
        drawable.setCornerRadius(mBGRadius);
        mBg.setBackgroundDrawable(drawable);

        params = new LayoutParams(mBGSize, 0);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        mSecondaryProgressBg = new View(context);
        addView(mSecondaryProgressBg, params);

        drawable = new GradientDrawable();
        drawable.setColor(0xffff5959);
        drawable.setCornerRadius(mBGRadius);
        mSecondaryProgressBg.setBackgroundDrawable(drawable);

        params = new LayoutParams(mBGSize, 0);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        mProgressBg = new View(context);
        addView(mProgressBg, params);

        drawable = new GradientDrawable();
        drawable.setColor(0xffff5959);
        drawable.setCornerRadius(mBGRadius);
        mProgressBg.setBackgroundDrawable(drawable);

        params = new LayoutParams(mCircleSize, mCircleSize);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        mSlider = new FrameLayout(context);
        addView(mSlider, params);

        drawable = new GradientDrawable();
        drawable.setColor(0xffdddddd);
        drawable.setShape(GradientDrawable.OVAL);

        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity=Gravity.CENTER;
        mTextView=new TextView(context);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextSize(12);
        mTextView.setIncludeFontPadding(false);
        mTextView.setText("0");
        mTextView.setTextColor(0xff666666);
        //mSlider.addView(mTextView, params);

        mSlider.setBackgroundDrawable(drawable);

        setClickable(true);
    }
    public String getText() {
        return mTextView.getText().toString();
    }

    public void setmText(String mText) {
        mTextView.setText(mText);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l)
    {
        mOnSeekBarChangeListener = l;
    }

    public void setThumb(Drawable d)
    {
        mSlider.setBackgroundDrawable(d);
    }

    public void setThumbSize(int w, int h)
    {
        LayoutParams params = (LayoutParams)mSlider.getLayoutParams();
        params.width = w;
        params.height = h;
        mSlider.setLayoutParams(params);
    }

    public void setProgressSize(int size)
    {
        LayoutParams params = (LayoutParams)mProgressBg.getLayoutParams();
        params.width = size;
        mProgressBg.setLayoutParams(params);

        params = (LayoutParams)mSecondaryProgressBg.getLayoutParams();
        params.width = size;
        mSecondaryProgressBg.setLayoutParams(params);

        params = (LayoutParams)mBg.getLayoutParams();
        params.width = size;
        mBg.setLayoutParams(params);
    }

    public void setProgressBackground(Drawable drawable)
    {
        mBg.setBackgroundDrawable(drawable);
    }

    public void setProgressDrawable(Drawable drawable)
    {
        mProgressBg.setBackgroundDrawable(drawable);
    }

    public void setSecondaryProgressDrawable(Drawable drawable)
    {
        mSecondaryProgressBg.setBackgroundDrawable(drawable);
    }

    public void setMax(int max)
    {
        mMax = max;
    }

    public void setProgress(int progress)
    {
        setProgress(progress, false);
    }

    public int getProgress()
    {
        return mProgress;
    }

    private void setProgress(int progress, boolean fromUser)
    {
        if(progress < 0)
            progress = 0;
        if(progress > mMax)
            progress = mMax;

        if(mProgress != progress)
        {
            mProgress = progress;
            updateProgress(fromUser);
        }
        mTextView.setText(progress+"");
    }

    private void updateProgress(boolean fromUser)
    {
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int h = getHeight() - paddingTop - paddingBottom;
        if(h > 0)
        {
            LayoutParams params = (LayoutParams)mProgressBg.getLayoutParams();
            params.height = h*mProgress/mMax;
            mProgressBg.setLayoutParams(params);

            params = (LayoutParams)mSlider.getLayoutParams();
            params.bottomMargin = (h-params.height)*mProgress/mMax;
            mSlider.setLayoutParams(params);
        }
        if(mOnSeekBarChangeListener != null){
            mOnSeekBarChangeListener.onProgressChanged(this, mProgress, fromUser);
        }
    }

    public void setSecondaryProgress(int progress)
    {
        if(progress < 0)
            progress = 0;
        if(progress > mMax)
            progress = mMax;

        mSecondaryProgress = progress;
        updateSecondaryProgress();
    }

    private void updateSecondaryProgress()
    {
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int h = getHeight() - paddingTop - paddingBottom;
        if(h > 0)
        {
            LayoutParams params = (LayoutParams)mSecondaryProgressBg.getLayoutParams();
            params.height = h*mSecondaryProgress/mMax;
            mSecondaryProgressBg.setLayoutParams(params);
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        int action = ev.getAction();
        int y = (int)ev.getY();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int h = getHeight() - paddingTop - paddingBottom;
        if(y < paddingTop){
            y = paddingTop;
        }
        if(y > h + paddingTop){
            y = h + paddingTop;
        }
        y -= paddingTop;
        if(action == MotionEvent.ACTION_DOWN)
        {
            if(mOnSeekBarChangeListener != null){
                mOnSeekBarChangeListener.onRequestDisallowInterceptTouchEvent(true);
            }
            int progress = mMax*(h-y)/h;
            if(progress != mProgress)
            {
                if(mTrackingTouch == false){
                    mTrackingTouch = true;
                    if(mOnSeekBarChangeListener != null){
                        mOnSeekBarChangeListener.onStartTrackingTouch(this);
                    }
                }
                setProgress(progress, true);
            }
        }
        else if(action == MotionEvent.ACTION_MOVE)
        {
            int progress = mMax*(h-y)/h;
            if(progress != mProgress)
            {
                if(mTrackingTouch == false){
                    mTrackingTouch = true;
                    if(mOnSeekBarChangeListener != null){
                        mOnSeekBarChangeListener.onStartTrackingTouch(this);
                    }
                }
                setProgress(progress, true);
            }
        }
        else if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
        {
            if(mOnSeekBarChangeListener != null){
                mOnSeekBarChangeListener.onRequestDisallowInterceptTouchEvent(false);
            }
            mTrackingTouch = false;
            if(mOnSeekBarChangeListener != null){
                mOnSeekBarChangeListener.onStopTrackingTouch(this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        updateProgress(false);
        //updateSecondaryProgress();
        super.onSizeChanged(w, h, oldw, oldh);
    }

}
