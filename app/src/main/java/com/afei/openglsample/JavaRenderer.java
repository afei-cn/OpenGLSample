package com.afei.openglsample;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class JavaRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "JavaRenderer";
    private Context mContext;
    private int mProgram;
    private int mPositionHandle;

    public JavaRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String vertexSource = ShaderUtils.loadFromAssets("vertex.vsh", mContext.getResources());
        String fragmentSource = ShaderUtils.loadFromAssets("fragment.fsh", mContext.getResources());
        mProgram = ShaderUtils.createProgram(vertexSource, fragmentSource);
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        Log.d(TAG, "mPositionHandle: " + mPositionHandle);
        GLES30.glClearColor(0, 0, 0, 1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        float[] vertices = new float[]{
                0.0f, 0.5f, 0,
                -0.5f, -0.5f, 0,
                0.5f, -0.5f, 0
        };
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // 必须要是 native order
        FloatBuffer vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0); // 这一行不要漏了

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        // 1. 选择使用的程序
        GLES30.glUseProgram(mProgram);
        // 2. 加载顶点数据
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);
    }

}
