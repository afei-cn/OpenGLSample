package com.afei.particles;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GLSurfaceView mGLSurfaceView;
    private ParticlesRenderer mRenderer;
    private boolean mRendererSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        if (detectOpenGLES20()) {
            mGLSurfaceView.setEGLContextClientVersion(2);
            mRenderer = new ParticlesRenderer(this);
            mGLSurfaceView.setRenderer(mRenderer);
            mRendererSet = true;
        } else {
            Log.e(TAG, "onCreate: OpenGL ES 2.0 not supported on device.");
        }

        mGLSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            float previousX;
            float previousY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    int action = event.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {
                        previousX = event.getX();
                        previousY = event.getY();
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        final float deltaX = event.getX() - previousX;
                        final float deltaY = event.getY() - previousY;
                        previousX = event.getX();
                        previousY = event.getY();

                        mGLSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                mRenderer.handleDrag(deltaX, deltaY);
                            }
                        });
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        setContentView(mGLSurfaceView);
    }

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
