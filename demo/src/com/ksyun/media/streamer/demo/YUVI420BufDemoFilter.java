package com.ksyun.media.streamer.demo;

import com.ksyun.media.streamer.filter.imgtex.ImgTexBufFilter;
import com.ksyun.media.streamer.framework.ImgBufFormat;
import com.ksyun.media.streamer.util.gles.GLRender;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Demo filter to handle YUV420 buffer in gpu pipe.
 */

public class YUVI420BufDemoFilter extends ImgTexBufFilter {
    private static final String TAG = "YUVI420BufDemoFilter";

    public YUVI420BufDemoFilter(GLRender glRender) {
        super(glRender, ImgBufFormat.FMT_I420);
    }

    @Override
    protected void onSizeChanged(int[] stride, int width, int height) {
        Log.d(TAG, "onSizeChanged " + stride[0] + " " + width + "x" + height);
        // do nothing.
    }

    @Override
    protected ByteBuffer doFilter(ByteBuffer buffer, int[] stride, int width, int height) {
        Log.d(TAG, "doFilter " + buffer + " " + stride[0] + " " + width + "x" + height);
        // handle buffer here
        return buffer;
    }
}
