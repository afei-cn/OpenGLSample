package com.afei.openglsample;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NativeRenderer implements GLSurfaceView.Renderer {

    private Context mContext;

    static {
        System.loadLibrary("native-renderer");
    }

    public NativeRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        registerAssetManager(mContext.getAssets());
        glInit();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glResize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glDraw();
    }

    public native void registerAssetManager(AssetManager assetManager);
    public native void glInit();
    public native void glResize(int width, int height);
    public native void glDraw();
}
