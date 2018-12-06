package com.afei.openglsample;

import android.content.Context;
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
        // vPosition 是在 'vertex.vsh' 文件中定义的
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        Log.d(TAG, "mPositionHandle: " + mPositionHandle);
        // 背景颜色设置为黑色 RGBA (range: 0.0 ~ 1.0)
        GLES30.glClearColor(0, 0, 0, 1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 视距区域设置使用 GLSurfaceView 的宽高
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        int vertexCount = 3;
        // OpenGL的世界坐标系是 [-1, -1, 1, 1]
        float[] vertices = new float[]{
                0.0f, 0.5f, 0, // 第一个点（x, y, z）
                -0.5f, -0.5f, 0, // 第二个点（x, y, z）
                0.5f, -0.5f, 0 // 第三个点（x, y, z）
        };
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4); // 一个 float 是四个字节
        vbb.order(ByteOrder.nativeOrder()); // 必须要是 native order
        FloatBuffer vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0); // 这一行不要漏了

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT); // clear color buffer
        // 1. 选择使用的程序
        GLES30.glUseProgram(mProgram);
        // 2. 加载顶点数据
        GLES30.glVertexAttribPointer(mPositionHandle, vertexCount, GLES30.GL_FLOAT, false, 3 * 4, vertexBuffer);
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        // 3. 绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);
    }

}
