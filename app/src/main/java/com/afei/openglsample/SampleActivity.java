package com.afei.openglsample;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SampleActivity extends AppCompatActivity {

    private static final String TAG = "SampleActivity";
    public static final String TYPE_NAME = "type";
    public static final int TYPE_NATIVE = 0;
    public static final int TYPE_JAVA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkOpenGLES30()) {
            Log.e(TAG, "con't support OpenGL ES 3.0!");
            finish();
        }
        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(getRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(glSurfaceView);
    }

    private GLSurfaceView.Renderer getRenderer() {
        Intent intent = getIntent();
        int type = intent.getIntExtra(TYPE_NAME, TYPE_NATIVE);
        GLSurfaceView.Renderer renderer = null;
        if (type == TYPE_NATIVE) {
            renderer = new NativeRenderer(this);
        } else {
            renderer = new NativeRenderer(this);
        }
        return renderer;
    }

    private boolean checkOpenGLES30() {
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x30000);
    }
}
