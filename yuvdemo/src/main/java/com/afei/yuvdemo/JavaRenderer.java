package com.afei.yuvdemo;

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
    private int[] mTextureIds;

    private int yuvWidth;
    private int yuvHeight;
    private ByteBuffer yBuffer;
    private ByteBuffer uBuffer;
    private ByteBuffer vBuffer;
    protected FloatBuffer mVertexBuffer;

    public JavaRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 视距区域设置使用 GLSurfaceView 的宽高
        GLES30.glViewport(0, 0, width, height);
    }

    private void init() {
        String vertexSource = ShaderUtil.loadFromAssets("vertex.vsh", mContext.getResources());
        String fragmentSource = ShaderUtil.loadFromAssets("fragment.fsh", mContext.getResources());
        mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
        //创建纹理
        mTextureIds = new int[3];
        GLES30.glGenTextures(mTextureIds.length, mTextureIds, 0);
        for (int i = 0; i < mTextureIds.length; i++) {
            //绑定纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureIds[i]);
            //设置环绕和过滤方式
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        }

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
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
    }

    public void setYuvData(byte[] i420, int width, int height) {
        if (yBuffer != null) yBuffer.clear();
        if (uBuffer != null) uBuffer.clear();
        if (vBuffer != null) vBuffer.clear();

        // 该函数多次被调用的时，不要每次都new，可以设置为全局变量缓存起来
        byte[] y = new byte[width * height];
        byte[] u = new byte[width * height / 4];
        byte[] v = new byte[width * height / 4];
        System.arraycopy(i420, 0, y, 0, y.length);
        System.arraycopy(i420, y.length, u, 0, u.length);
        System.arraycopy(i420, y.length + u.length, v, 0, v.length);
        yBuffer = ByteBuffer.wrap(y);
        uBuffer = ByteBuffer.wrap(u);
        vBuffer = ByteBuffer.wrap(v);
        yuvWidth = width;
        yuvHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (yBuffer == null || uBuffer == null || vBuffer == null) {
            return;
        }
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT); // clear color buffer
        // 1. 选择使用的程序
        GLES30.glUseProgram(mProgram);
        // 2.1 加载纹理y
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0); //激活纹理0
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureIds[0]); //绑定纹理
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, yuvWidth,
                yuvHeight, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, yBuffer); // 赋值
        GLES30.glUniform1i(0, 0); // sampler_y的location=0, 把纹理0赋值给sampler_y
        // 2.2 加载纹理u
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureIds[1]);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, yuvWidth / 2,
                yuvHeight / 2, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, uBuffer);
        GLES30.glUniform1i(1, 1); // sampler_u的location=1, 把纹理1赋值给sampler_u
        // 2.3 加载纹理v
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureIds[2]);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, yuvWidth / 2,
                yuvHeight / 2, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, vBuffer);
        GLES30.glUniform1i(2, 2); // sampler_v的location=2, 把纹理1赋值给sampler_v
        // 3. 加载顶点数据
        mVertexBuffer.position(0);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 5 * 4, mVertexBuffer);
        GLES30.glEnableVertexAttribArray(0);
        mVertexBuffer.position(3);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 5 * 4, mVertexBuffer);
        GLES30.glEnableVertexAttribArray(1);
        // 4. 绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);
    }

}
