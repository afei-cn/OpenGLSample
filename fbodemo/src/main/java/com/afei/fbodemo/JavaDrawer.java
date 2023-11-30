package com.afei.fbodemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class JavaDrawer {

    private static final String TAG = "JavaDrawer";

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBitmapBuffer;
    private FloatBuffer mTextureRotation0Buffer;

    private ByteBuffer mDrawListBuffer;
    private int mKernelSize = 7;

    private static final float VERTEXES[] = {
            -1.0f, 1.0f,  // 左上角
            -1.0f, -1.0f,  // 左下角
            1.0f, -1.0f,  // 右上角
            1.0f, 1.0f   // 右下角
    };

    // 在Android屏幕上(0, 0)点是在屏幕左上角，所以Bitmap绘制的时候需要上下镜像才能正确显示
    private static final float TEXTURE_BITMAP[] = {
            0.0f, 0.0f,  // 左下角
            0.0f, 1.0f,  // 左上角
            1.0f, 1.0f,  // 右下角
            1.0f, 0.0f   // 右上角
    };

    private static final float TEXTURE_ROTATION_0[] = {
            0.0f, 1.0f,  // 左上角
            0.0f, 0.0f,  // 左下角
            1.0f, 0.0f,  // 右上角
            1.0f, 1.0f   // 右下角
    };

    private int mGLProgram;
    private int[] mTextures = new int[2];
    private int[] mFBO = new int[1];

    private static final byte VERTEX_ORDER[] = {0, 1, 2, 3}; // order to draw vertices

    private final int VERTEX_SIZE = 2;
    private final int VERTEX_STRIDE = VERTEX_SIZE * 4;

    public JavaDrawer() {
        // init float buffer for vertex coordinates
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEXES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertexBuffer.put(VERTEXES).position(0);
        // init float buffer for texture coordinates
        mTextureBitmapBuffer =
                ByteBuffer.allocateDirect(TEXTURE_BITMAP.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureBitmapBuffer.put(TEXTURE_BITMAP).position(0);
        mTextureRotation0Buffer =
                ByteBuffer.allocateDirect(TEXTURE_ROTATION_0.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureRotation0Buffer.put(TEXTURE_ROTATION_0).position(0);
        // init byte buffer for draw list
        mDrawListBuffer = ByteBuffer.allocateDirect(VERTEX_ORDER.length).order(ByteOrder.nativeOrder());
        mDrawListBuffer.put(VERTEX_ORDER).position(0);
    }

    public void init(Context context, Bitmap bitmap, int viewWidth, int viewHeight) {
        if (bitmap == null) {
            return;
        }
        String vertexSource = OpenGLUtil.loadFromAssets("vertex.vsh", context.getResources());
        String fragmentSource = OpenGLUtil.loadFromAssets("boxfilter.fsh", context.getResources());
        mGLProgram = OpenGLUtil.createProgram(vertexSource, fragmentSource);
        Log.d(TAG, "createProgram id = " + mGLProgram);
        GLES30.glGenFramebuffers(mFBO.length, mFBO, 0);
        OpenGLUtil.createTextures(mTextures);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextures[0]);  // 纹理0存放图片数据
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextures[1]);  // 纹理1存放FBO颜色附着
        // 这里申请的纹理内存大小要和glViewport()的大小保持一致，否则FBO输出的颜色数据可能存放不完整
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, viewWidth, viewHeight, 0, GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE, null);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    public void unInit() {
        GLES30.glDeleteFramebuffers(mFBO.length, mFBO, 0);
        GLES30.glDeleteTextures(mTextures.length, mTextures, 0);
        GLES30.glDeleteProgram(mGLProgram);
    }

    public void drawBitmap(boolean blurFlag) {
        if (mGLProgram <= 0) {
            Log.e(TAG, "mGLProgram not create!");
            return;
        }
        GLES30.glFinish();
        long startTime = System.currentTimeMillis();

        Log.d(TAG, "drawBitmap mGLProgram: " + mGLProgram);
        GLES30.glUseProgram(mGLProgram); // 指定使用的program
        GLES30.glEnable(GLES30.GL_CULL_FACE); // 启动剔除
        // init vertex
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(0, VERTEX_SIZE, GLES30.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);
        GLES30.glVertexAttribPointer(1, VERTEX_SIZE, GLES30.GL_FLOAT, false, VERTEX_STRIDE, mTextureBitmapBuffer);
        // bind texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextures[0]);
        GLES30.glUniform1i(2, 0);
        if (blurFlag) {
            GLES30.glUniform1i(3, mKernelSize);  // set u_kernelSize
            GLES30.glUniform1i(4, 1);  // set u_boxFilterType
        } else {
            GLES30.glUniform1i(3, 0);  // set u_kernelSize
            GLES30.glUniform1i(4, 0);  // set u_boxFilterType
        }
        GLES30.glDrawElements(GLES30.GL_TRIANGLE_FAN, VERTEX_ORDER.length, GLES30.GL_UNSIGNED_BYTE, mDrawListBuffer);
//        GLES30.glDisableVertexAttribArray(0);
//        GLES30.glDisableVertexAttribArray(1);

        GLES30.glFinish();
        long endTime = System.currentTimeMillis();
        Log.i(TAG, "drawBitmap time(ms): " + (endTime - startTime));
    }

    public void drawBitmapUseFBO() {
        if (mGLProgram <= 0) {
            Log.e(TAG, "mGLProgram not create!");
            return;
        }
        GLES30.glFinish();
        long startTime = System.currentTimeMillis();

        Log.d(TAG, "drawBitmapUseFBO mGLProgram: " + mGLProgram);
        GLES30.glUseProgram(mGLProgram); // 指定使用的program
        GLES30.glEnable(GLES30.GL_CULL_FACE); // 启动剔除
        // init vertex
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(0, VERTEX_SIZE, GLES30.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);
        GLES30.glVertexAttribPointer(1, VERTEX_SIZE, GLES30.GL_FLOAT, false, VERTEX_STRIDE, mTextureBitmapBuffer);

        // 先进行boxFilterHorizontal，渲染到FBO上，绑定颜色附着的纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextures[0]);
        GLES30.glUniform1i(2, 0);
        GLES30.glUniform1i(3, mKernelSize);  // set u_kernelSize
        GLES30.glUniform1i(4, 2);  // set u_boxFilterType to boxFilterHorizontal
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFBO[0]);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, mTextures[1],
                0);
        GLES30.glDrawElements(GLES30.GL_TRIANGLE_FAN, VERTEX_ORDER.length, GLES30.GL_UNSIGNED_BYTE, mDrawListBuffer);

        // 再进行boxFilterVertical，渲染到屏幕
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);  // 解绑，即重新绑定回屏幕
//        GLES30.glVertexAttribPointer(0, VERTEX_SIZE, GLES30.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);
        // 这次不需要再对纹理进行上下翻转了，重新设置下纹理坐标的值
        GLES30.glVertexAttribPointer(1, VERTEX_SIZE, GLES30.GL_FLOAT, false, VERTEX_STRIDE, mTextureRotation0Buffer);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextures[1]);
        GLES30.glUniform1i(2, 1);
        GLES30.glUniform1i(3, mKernelSize);  // set u_kernelSize
        GLES30.glUniform1i(4, 3);  // set u_boxFilterType to boxFilterVertical
        GLES30.glDrawElements(GLES30.GL_TRIANGLE_FAN, VERTEX_ORDER.length, GLES30.GL_UNSIGNED_BYTE, mDrawListBuffer);
//        GLES30.glDisableVertexAttribArray(0);
//        GLES30.glDisableVertexAttribArray(1);

        GLES30.glFinish();
        long endTime = System.currentTimeMillis();
        Log.i(TAG, "drawBitmapUseFBO time(ms): " + (endTime - startTime));
    }

    public void drawBitmapUseMRT() {

    }

}
