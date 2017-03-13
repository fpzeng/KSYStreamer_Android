package com.ksyun.media.streamer.demo;

import com.ksyun.media.streamer.capture.ImgTexSrcPin;
import com.ksyun.media.streamer.filter.imgtex.ImgFilterBase;
import com.ksyun.media.streamer.framework.ImgTexFrame;
import com.ksyun.media.streamer.framework.SinkPin;
import com.ksyun.media.streamer.framework.SrcPin;
import com.ksyun.media.streamer.util.gles.GLRender;

/**
 * Created by qyvideo on 2/23/17.
 */

public class ImgTexCustomFilter extends ImgFilterBase{

    private SinkPin<ImgTexFrame> mSinkPin;
    private ImgTexSrcPin mImgTexSrcPin;
    private GLRender mGLRender;

    public ImgTexCustomFilter(GLRender glRender) {
        mGLRender = glRender;
        mSinkPin = new SinkPin<ImgTexFrame>() {
            @Override
            public void onFormatChanged(Object format) {
                mImgTexSrcPin.onFormatChanged(format);
            }

            @Override
            public void onFrameAvailable(ImgTexFrame frame) {
                /*texture id: frame.textureId;
                    width: frame.format.width;
                    height: frame.format.height
                 */

                int newTextureID = 0; // you must allocate a new one yourself, do not use this zero

                //do sticker job here!

                final ImgTexFrame newFrame = new ImgTexFrame(frame.format, newTextureID, frame
                        .texMatrix, frame.pts);
                mGLRender.queueDrawFrameAppends(new Runnable() {
                    @Override
                    public void run() {
                        mImgTexSrcPin.onFrameAvailable(newFrame);
                    }
                });
            }

            @Override
            public void onDisconnect(boolean recursive) {
                mImgTexSrcPin.disconnect(recursive);
            }
        };
        mImgTexSrcPin = new ImgTexSrcPin(glRender);
    }

    @Override
    public int getSinkPinNum() {
        return 1;
    }

    @Override
    public SinkPin<ImgTexFrame> getSinkPin(int idx) {
        return mSinkPin;
    }

    @Override
    public SrcPin<ImgTexFrame> getSrcPin() {
        return mImgTexSrcPin;
    }
}
