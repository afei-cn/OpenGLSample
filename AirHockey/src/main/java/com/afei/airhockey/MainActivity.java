package com.afei.airhockey;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GLSurfaceView mGLSurfaceView;
    private boolean mRendererSet = false;

    private AirHockeyRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        if (detectOpenGLES20()) {
            mGLSurfaceView.setEGLContextClientVersion(2);
            mRenderer = new AirHockeyRenderer(this);
            mGLSurfaceView.setRenderer(mRenderer);
//            mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            mGLSurfaceView.setOnTouchListener(mOnTouchListener);
            mRendererSet = true;
        } else {
            Log.e(TAG, "onCreate: OpenGL ES 2.0 not supported on device.");
        }
        setContentView(mGLSurfaceView);
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event != null) {
                final float normalizedX = (event.getX() / (float) v.getWidth()) * 2 - 1;
                final float normalizedY = -((event.getY() / (float) v.getHeight()) * 2 - 1);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mGLSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mRenderer.handleTouchPress(normalizedX, normalizedY);
                        }
                    });
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    mGLSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            mRenderer.handleTouchDrag(normalizedX, normalizedY);
                        }
                    });
                }
                return true;
            } else {
                return false;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (mRendererSet) {
            mGLSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRendererSet) {
            mGLSurfaceView.onResume();
        }
    }

    private boolean detectOpenGLES20() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x20000);
    }
}
