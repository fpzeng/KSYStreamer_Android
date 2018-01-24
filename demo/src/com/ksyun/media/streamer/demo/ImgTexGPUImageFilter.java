package com.ksyun.media.streamer.demo;

import android.util.Log;

import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterBase;
import com.ksyun.media.streamer.framework.ImgTexFormat;
import com.ksyun.media.streamer.framework.ImgTexFrame;
import com.ksyun.media.streamer.util.gles.GLRender;
import com.ksyun.media.streamer.util.gles.GlUtil;
import com.ksyun.media.streamer.util.gles.TexTransformUtil;

import java.nio.FloatBuffer;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Wrapper to compat with android-gpuimage filters.
 */

public class ImgTexGPUImageFilter extends ImgTexFilterBase {
    private static final String TAG = "ImgTexGPUImageFilter";

    private GPUImageFilter mGPUImageFilter;
    private ImgTexFormat mOutFormat;

    public ImgTexGPUImageFilter(GLRender glRender, GPUImageFilter gpuImageFilter) {
        super(glRender);
        mGPUImageFilter = gpuImageFilter;
    }

    public GPUImageFilter getGPUImageFilter() {
        return mGPUImageFilter;
    }

    @Override
    public int getSinkPinNum() {
        return 1;
    }

    @Override
    protected ImgTexFormat getSrcPinFormat() {
        return mOutFormat;
    }

    @Override
    protected void onFormatChanged(int inIdx, ImgTexFormat format) {
        mOutFormat = new ImgTexFormat(ImgTexFormat.COLOR_RGBA, format.width, format.height);
        Log.d(TAG, "init GPUImage filter " + format.width + "x" + format.height);
        mGPUImageFilter.init();
        mGPUImageFilter.onOutputSizeChanged(format.width, format.height);
    }

    @Override
    protected void onDraw(ImgTexFrame[] frames) {
        int textureId = frames[mMainSinkPinIndex].textureId;
        mGPUImageFilter.onDraw(textureId, getVertexCoords(), getTexCoords());
        GlUtil.checkGlError("GPUImageFilter onDraw");
    }

    @Override
    protected void onRelease() {
        mGPUImageFilter.destroy();
        super.onRelease();
    }

    protected FloatBuffer getTexCoords() {
        return TexTransformUtil.getTexCoordsBuf();
    }

    protected FloatBuffer getVertexCoords() {
        return TexTransformUtil.getVertexCoordsBuf();
    }
}
