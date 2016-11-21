package com.ksyun.media.streamer.demo;


import android.opengl.GLES20;

import com.ksyun.media.streamer.filter.imgtex.ImgTexFilter;
import com.ksyun.media.streamer.util.gles.GLRender;

/**
 * Demo filter.
 */
public class DemoFilter extends ImgTexFilter {
    // Fragment shader that attempts to produce a high contrast image
    private static final String FRAGMENT_SHADER_BODY =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform  float greenplus;\n" +
                    "void main() {\n" +
                    "    vec4 tc = texture2D(sTexture, vTextureCoord);\n" +
                    "    float color = ((tc.r  + tc.g + tc.b ) / 3.0) ;\n" +
                    "    gl_FragColor = vec4(color, color + greenplus, color, 1.0);\n" +
                    "}\n";

    public DemoFilter(GLRender glRender) {
        super(glRender, BASE_VERTEX_SHADER, FRAGMENT_SHADER_BODY);
    }

    public void onInitialized() {
        int greenplusLocation = getUniformLocation("greenplus");
        GLES20.glUniform1f(greenplusLocation, 0.3f);
    }
}
