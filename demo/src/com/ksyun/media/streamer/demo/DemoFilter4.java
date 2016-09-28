package com.ksyun.media.streamer.demo;


import android.opengl.GLES20;

import com.ksyun.media.streamer.filter.imgtex.GlUtil;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilter;

/**
 * Demo filter.
 */
public class DemoFilter4 extends ImgTexFilter {
    // Fragment shader that attempts to produce a high contrast image
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform  float greenplus;\n" +
                    "void main() {\n" +
                    "    vec4 tc = texture2D(sTexture, vTextureCoord);\n" +
                    "    float color = ((tc.r  + tc.g + tc.b ) / 3.0) ;\n" +
                    "    gl_FragColor = vec4(color, color + greenplus, color, 1.0);\n" +
                    "}\n";

    public DemoFilter4() {
        super(GlUtil.BASE_VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public void onInitialized() {
        int greenplusLocation = getUniformLocation("greenplus");
        GLES20.glUniform1f(greenplusLocation, 0.3f);
    }

}
