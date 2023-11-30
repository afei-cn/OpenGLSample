/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.afei.fbodemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * A {@link GLSurfaceView} that can be adjusted to a specified aspect ratio.
 */
public class AutoFitGLSurfaceView extends GLSurfaceView {

    private static final String TAG = "AutoFitGLSurfaceView";
    public static final int RENDER_BITMAP_ORIGIN = 0;
    public static final int RENDER_BITMAP_BLUR = 1;
    public static final int RENDER_BITMAP_BLUR_FBO = 2;

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private Context mContext;
    private JavaDrawer mJavaDrawer;
    private Bitmap mBitmap;
    private int mRenderMode = RENDER_BITMAP_ORIGIN;

    public AutoFitGLSurfaceView(Context context) {
        this(context, null);
    }

    public AutoFitGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test_img);
        setAspectRatio(mBitmap.getWidth(), mBitmap.getHeight());

        setEGLContextClientVersion(3);
        setRenderer(new Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                mJavaDrawer.init(mContext, mBitmap, getWidth(), getHeight());
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES30.glViewport(0, 0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                if (mRenderMode == RENDER_BITMAP_BLUR) {
                    mJavaDrawer.drawBitmap( true);
                } else if (mRenderMode == RENDER_BITMAP_BLUR_FBO) {
                    mJavaDrawer.drawBitmapUseFBO();
                } else {  // RENDER_BITMAP_ORIGIN
                    mJavaDrawer.drawBitmap(false);
                }
            }
        });
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mJavaDrawer = new JavaDrawer();
    }

    public void drawBitmap(boolean blurFlag) {
        mRenderMode = blurFlag ? RENDER_BITMAP_BLUR : RENDER_BITMAP_ORIGIN;
        requestRender();  // 请求渲染
    }

    public void drawBitmapUseFBO() {
        mRenderMode = RENDER_BITMAP_BLUR_FBO;
        requestRender();  // 请求渲染
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        post(() -> requestLayout());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

}
