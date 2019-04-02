package com.afei.particles.wallpaper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import com.afei.particles.ParticlesRenderer;

public class GLWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }

    public class GLEngine extends WallpaperService.Engine {

        private static final String TAG = "GLEngine";
        private WallpaperGLSurfaceView mGLSurfaceView;
        private ParticlesRenderer mRenderer;
        private boolean mRendererSet;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            mGLSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);
            if (detectOpenGLES20()) {
                mGLSurfaceView.setEGLContextClientVersion(2);
                mRenderer = new ParticlesRenderer(GLWallpaperService.this);
                mGLSurfaceView.setPreserveEGLContextOnPause(true); // 保留EGL上下文信息
                mGLSurfaceView.setRenderer(mRenderer);
                mRendererSet = true;
            } else {
                Log.e(TAG, "onCreate: OpenGL ES 2.0 not supported on device.");
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (mRendererSet) {
                if (visible) {
                    mGLSurfaceView.onResume();
                } else {
                    mGLSurfaceView.onPause();
                }
            }
            super.onVisibilityChanged(visible);
        }

        @Override
        public void onOffsetsChanged(final float xOffset, final float yOffset, float xOffsetStep, float yOffsetStep, int
                xPixelOffset, int yPixelOffset) {
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mRenderer.handleOffsetsChanged(xOffset, yOffset);
                }
            });
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mGLSurfaceView.onWallpaperDestroy();
        }

        class WallpaperGLSurfaceView extends GLSurfaceView {
            private static final String TAG = "WallpaperGLSurfaceView";

            WallpaperGLSurfaceView(Context context) {
                super(context);
                Log.d(TAG, "WallpaperGLSurfaceView(" + context + ")");
            }

            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }

            public void onWallpaperDestroy() {
                super.onDetachedFromWindow();
            }
        }
    }

    private boolean detectOpenGLES20() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x20000);
    }

}
