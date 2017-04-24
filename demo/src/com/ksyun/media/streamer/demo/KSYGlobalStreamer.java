package com.ksyun.media.streamer.demo;

import android.annotation.SuppressLint;

import com.ksyun.media.streamer.kit.KSYStreamer;

/**
 * Class to store global streaming instance.
 */

public class KSYGlobalStreamer {

    @SuppressLint("StaticFieldLeak")
    private static KSYStreamer sStreamer;

    // private constructor
    private KSYGlobalStreamer() {

    }

    public static void setInstance(KSYStreamer streamer) {
        sStreamer = streamer;
    }

    public static KSYStreamer getInstance() {
        return sStreamer;
    }
}
