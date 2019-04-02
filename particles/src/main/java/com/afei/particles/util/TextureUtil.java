package com.afei.particles.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class TextureUtil {

    public static final String TAG = "TextureUtil";

    public static int loadTexture(Context context, int resId) {
        final int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL texture object");
            return 0;
        }

        // load bitmap
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, options);
        if (bitmap == null) {
            Log.e(TAG, "resource id: " + resId + " could not be decoded");
            GLES20.glDeleteTextures(1, textureIds, 0);
            return 0;
        }

        // bind
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        // 缩小的情况，使用三线性过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        // 放大的情况，使用双线性过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 加载位图到OpenGL中
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        // unbind
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        bitmap.recycle();

        return textureIds[0];
    }

    /**
     * Loads a cubemap texture from the provided resources and returns the
     * texture ID. Returns 0 if the load failed.
     *
     * @param context
     * @param resIds  An array of resources corresponding to the cube map. Should be
     *                provided in this order: left, right, bottom, top, front, back.
     * @return
     */
    public static int loadCubeMap(Context context, int[] resIds) {
        final int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL texture object");
            return 0;
        }

        // load bitmap
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap[] bitmaps = new Bitmap[resIds.length];

        for (int i = 0; i < resIds.length; i++) {
            bitmaps[i] = BitmapFactory.decodeResource(context.getResources(), resIds[i], options);
            if (bitmaps[i] == null) {
                Log.e(TAG, "resource id: " + resIds[i] + " could not be decoded");
                GLES20.glDeleteTextures(1, textureIds, 0);
                return 0;
            }
        }

        // bind
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 加载位图到OpenGL中
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, bitmaps[0], 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, bitmaps[1], 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, bitmaps[2], 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, bitmaps[3], 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, bitmaps[4], 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, bitmaps[5], 0);

        // unbind
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, 0);
        for (Bitmap bitmap : bitmaps) {
            bitmap.recycle();
        }

        return textureIds[0];
    }

}
