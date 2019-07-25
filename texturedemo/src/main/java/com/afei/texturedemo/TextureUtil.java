package com.afei.texturedemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TextureUtil {

    public static final String TAG = "TextureUtil";

    public static int loadBitmapTexture(Context context, int resId) {
        final int[] textureId = new int[1];
        GLES30.glGenTextures(1, textureId, 0);
        if (textureId[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL texture object");
            return 0;
        }
        // load bitmap
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, options);
        if (bitmap == null) {
            Log.e(TAG, "resource id: " + resId + " could not be decoded");
            GLES30.glDeleteTextures(1, textureId, 0);
            return 0;
        }
        // bind
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId[0]);
        // 缩小的情况，使用三线性过滤
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        // 放大的情况，使用双线性过滤
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        // 加载位图到OpenGL中
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);
        bitmap.recycle();
        // unbind
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        return textureId[0];
    }

    public static int loadTexture() {
        final int[] textureId = new int[1];
        GLES30.glGenTextures(1, textureId, 0);
        if (textureId[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL texture object");
            return 0;
        }
        // 2*2 RGB data for test
        float[] pixels = {
                1f, 0f, 0f, // Red
                0f, 1f, 0f, // Green
                0f, 0f, 1f, // Green
                1f, 1f, 0f  // Yellow
        };
        FloatBuffer pixelBuffer = ByteBuffer.allocateDirect(pixels.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(pixels);
        pixelBuffer.position(0);

        // bind
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId[0]);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        // 加载位图到OpenGL中
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB, 2, 2, 0, GLES30.GL_RGB, GLES30.GL_FLOAT, pixelBuffer);
        // unbind，绑0就是解绑
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        return textureId[0];
    }


}
