package com.afei.texturedemo;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class JavaRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "JavaRenderer";
    private Context mContext;
    private int mProgram;

    public JavaRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String vertexSource = ShaderUtils.loadFromAssets("vertex.vsh", mContext.getResources());
        String fragmentSource = ShaderUtils.loadFromAssets("fragment.fsh", mContext.getResources());
        mProgram = ShaderUtils.createProgram(vertexSource, fragmentSource);
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
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT); // clear color buffer
        // OpenGL的世界坐标系是 [-1, -1, 1, 1]，纹理的坐标系为 [0, 0, 1, 1]
        float[] vertices = new float[]{
                // 前三个数字为顶点坐标(x, y, z)，后两个数字为纹理坐标(s, t)
                // 第一个三角形
                1f,  1f,  0f,       1f, 0f,
                1f,  -1f, 0f,       1f, 1f,
                -1f, -1f, 0f,       0f, 1f,
                // 第二个三角形
                1f,  1f,  0f,       1f, 0f,
                -1f, -1f, 0f,       0f, 1f,
                -1f, 1f,  0f,       0f, 0f
        };
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4); // 一个 float 是四个字节
        vbb.order(ByteOrder.nativeOrder()); // 必须要是 native order
        FloatBuffer vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        // 1. 选择使用的程序
        GLES30.glUseProgram(mProgram);
        // 2. 加载纹理
        int textureId = TextureUtil.loadBitmapTexture(mContext, R.mipmap.test);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0); // 激活TEXTURE0
        int sTextureLocation = GLES30.glGetUniformLocation(mProgram, "s_texture");
        GLES30.glUniform1i(sTextureLocation, 0); // 因为激活的是TEXTURE0，所以要给这个纹理赋值0
        // 3. 加载顶点数据
        vertexBuffer.position(0);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 5 * 4, vertexBuffer);
        GLES30.glEnableVertexAttribArray(0);
        vertexBuffer.position(3);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 5 * 4, vertexBuffer);
        GLES30.glEnableVertexAttribArray(1);
        // 4. 绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);
    }

}
