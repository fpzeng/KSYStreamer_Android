package com.ksyun.media.streamer.demo;

import com.ksyun.media.streamer.filter.audio.AudioFilterBase;
import com.ksyun.media.streamer.framework.AudioBufFormat;
import com.ksyun.media.streamer.framework.AudioBufFrame;

import java.nio.ShortBuffer;

/**
 * demo audio filter
 * make voice lound
 */

public class DemoAudioFilter extends AudioFilterBase {
    private AudioBufFormat mAudioFormat;
    private float mVoiceVolume = 2.0f;

    /**
     * audio format changed
     *
     * @param format the changed format
     * @return the changed format
     */
    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        mAudioFormat = format;
        return format;
    }

    /**
     * process your audio in this function
     *
     * @param frame the input frame (directBuffer)
     * @return return the frame after process
     */
    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        //process frame
        if (mVoiceVolume != 1.0f) {
            ShortBuffer dstShort = frame.buf.asShortBuffer();
            for (int i = 0; i < dstShort.limit(); i++) {
                dstShort.put(i, (short) (dstShort.get(i) * mVoiceVolume));
            }
            dstShort.rewind();
        }
        return frame;
    }

    /**
     * release your resource in this function
     */
    @Override
    protected void doRelease() {

    }
}
