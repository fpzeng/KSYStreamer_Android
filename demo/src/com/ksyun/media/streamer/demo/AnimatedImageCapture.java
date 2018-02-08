package com.ksyun.media.streamer.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableFrameInfo;
import com.facebook.imagepipeline.animated.base.AnimatedImage;
import com.facebook.imagepipeline.animated.base.AnimatedImageFrame;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.ksyun.media.streamer.capture.ImgTexSrcPin;
import com.ksyun.media.streamer.framework.ImgTexFrame;
import com.ksyun.media.streamer.framework.SrcPin;
import com.ksyun.media.streamer.util.gles.GLRender;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class to decode animated GIF/WEBP.
 */

public class AnimatedImageCapture {
    private static final String TAG = "AnimatedImageCapture";
    private static final boolean VERBOSE = false;

    private int mIndex;
    private Date mLastDate;
    private int mRepeatCount;   // TODO: handle gif/webp repeat count
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Bitmap mTempBitmap;
    private Timer mTimer;
    private CloseableReference<? extends CloseableImage> mCloseableReference;
    private final Object mLock = new Object();

    private ImgTexSrcPin mImgTexSrcPin;

    public AnimatedImageCapture(GLRender glRender) {
        mImgTexSrcPin = new ImgTexSrcPin(glRender);
        mImgTexSrcPin.setUseSyncMode(true);
    }

    public SrcPin<ImgTexFrame> getSrcPin() {
        return mImgTexSrcPin;
    }

    public void start(Context context, String url) {
        if (url == null) {
            return;
        }

        Uri uri = Uri.parse(assets2Asset(url));
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri).build();
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchDecodedImage(request, context);
        dataSource.subscribe(mDataSubscriber, CallerThreadExecutor.getInstance());
    }

    private String assets2Asset(String url) {
        if (url.startsWith("assets://")) {
            url = url.replaceFirst("assets://", "asset:///");
        }
        return url;
    }

    public void stop() {
        synchronized (mLock) {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            if (mCloseableReference != null) {
                CloseableReference.closeSafely(mCloseableReference);
                mCloseableReference = null;
            }
        }
        mImgTexSrcPin.updateFrame(null, false);
    }

    public void release() {
        stop();
        mImgTexSrcPin.release();
    }

    private void updateFrame() {
        synchronized (mLock) {
            if (mCloseableReference == null) {
                return;
            }
            try {
                CloseableImage closeableImage = mCloseableReference.get();
                if (closeableImage instanceof CloseableBitmap) {
                    Bitmap bitmap = ((CloseableBitmap) closeableImage).getUnderlyingBitmap();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        mBitmap = Bitmap.createBitmap(bitmap);
                        mImgTexSrcPin.updateFrame(mBitmap, false);
                    }
                    return;
                }

                if (!(closeableImage instanceof CloseableAnimatedImage)) {
                    return;
                }
                AnimatedImage animatedImage = ((CloseableAnimatedImage) closeableImage).getImage();
                int w = animatedImage.getWidth();
                int h = animatedImage.getHeight();
                if (mBitmap == null || mBitmap.isRecycled()) {
                    Log.d(TAG, "Got animated image " + w + "x" + h);
                    mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    mTempBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    mCanvas = new Canvas(mBitmap);
                }
                if (mIndex >= animatedImage.getFrameCount()) {
                    mRepeatCount++;
                    mIndex = 0;
                    int loopCount = animatedImage.getLoopCount();
                    if (loopCount > 0 && mRepeatCount >= loopCount) {
                        Log.d(TAG, "repeat ended, repeat times: " + mRepeatCount);
                        return;
                    }
                }

                AnimatedImageFrame imageFrame = animatedImage.getFrame(mIndex);
                int duration = imageFrame.getDurationMs();
                AnimatedDrawableFrameInfo frameInfo = animatedImage.getFrameInfo(mIndex);
                imageFrame.renderFrame(frameInfo.width, frameInfo.height, mTempBitmap);
                if (VERBOSE) {
                    Log.d(TAG, "blend: " + frameInfo.blendOperation.name() +
                            " dispose: " + frameInfo.disposalMethod.name() +
                            " x=" + frameInfo.xOffset + " y=" + frameInfo.yOffset +
                            " " + frameInfo.width + "x" + frameInfo.height);
                }
                if (frameInfo.blendOperation == AnimatedDrawableFrameInfo.BlendOperation.NO_BLEND) {
                    mBitmap.eraseColor(Color.TRANSPARENT);
                }
                Rect srcRect = new Rect(0, 0, frameInfo.width, frameInfo.height);
                Rect dstRect = new Rect(frameInfo.xOffset, frameInfo.yOffset,
                        frameInfo.xOffset + frameInfo.width,
                        frameInfo.yOffset + frameInfo.height);
                mCanvas.drawBitmap(mTempBitmap, srcRect, dstRect, null);
                if (VERBOSE) {
                    Log.d(TAG, "frame " + mIndex + " got, duration=" + duration);
                }
                mImgTexSrcPin.updateFrame(mBitmap, false);
                imageFrame.dispose();

                mIndex++;
                if (mLastDate == null) {
                    mLastDate = new Date();
                }
                mLastDate = new Date(mLastDate.getTime() + duration);
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateFrame();
                    }
                }, mLastDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private DataSubscriber mDataSubscriber =
            new BaseDataSubscriber<CloseableReference<? extends CloseableImage>>() {

        @Override
        public void onNewResultImpl(
                DataSource<CloseableReference<? extends CloseableImage>> dataSource) {
            if (!dataSource.isFinished()) {
                return;
            }

            synchronized (mLock) {
                // get ref
                mCloseableReference = dataSource.getResult();

                // reset
                mIndex = 0;
                mRepeatCount = 0;
                mLastDate = null;
                mBitmap = null;
                mTempBitmap = null;
                mCanvas = null;

                // create timer
                mTimer = new Timer("AnimatedImage");
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateFrame();
                    }
                }, 0);
            }
        }

        @Override
        public void onFailureImpl(DataSource dataSource) {
            Throwable throwable = dataSource.getFailureCause();
            // handle failure
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }
    };
}
