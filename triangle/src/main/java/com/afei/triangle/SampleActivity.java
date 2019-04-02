package com.afei.triangle;

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

    private GLSurfaceView mGlSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkOpenGLES30()) {
            Log.e(TAG, "con't support OpenGL ES 3.0!");
            finish();
        }
        mGlSurfaceView = new GLSurfaceView(this);
        mGlSurfaceView.setEGLContextClientVersion(3);
        mGlSurfaceView.setRenderer(getRenderer());
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(mGlSurfaceView);
    }

    private GLSurfaceView.Renderer getRenderer() {
        Intent intent = getIntent();
        int type = intent.getIntExtra(TYPE_NAME, TYPE_NATIVE);
        Log.d(TAG, "type: " + type);
        GLSurfaceView.Renderer renderer;
        if (type == TYPE_NATIVE) {
            renderer = new NativeRenderer(this);
        } else {
            renderer = new JavaRenderer(this);
        }
        return renderer;
    }

    private boolean checkOpenGLES30() {
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x30000);
    }

    @Override
    protected void onPause() {
        mGlSurfaceView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mGlSurfaceView.onResume();
        super.onResume();
    }
}
