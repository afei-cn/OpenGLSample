package com.afei.yuvdemo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private GLSurfaceView mGlSurfaceView;
    private JavaRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkOpenGLES30()) {
            Log.e(TAG, "con't support OpenGL ES 3.0!");
            finish();
        }
        mGlSurfaceView = new GLSurfaceView(this);
        mGlSurfaceView.setEGLContextClientVersion(3); // 设置OpenGL版本号
        mRenderer = new JavaRenderer(this);
        mGlSurfaceView.setRenderer(mRenderer);
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // 设置渲染模式为仅当手动执行requestRender时才绘制
        setContentView(mGlSurfaceView);
        // 实际场景中，可能是在相机预览回调或解码回调中调用，这里仅使用预设的yuv图片做示例
        drawYuv();
    }

    private void drawYuv() {
        // 绘制的width必须是8的倍数，height必须是2的倍数，如果不是则需要对齐到8的倍数，否则渲染的结果不对
        byte[] i420 = FileUtil.getAssertData(this, "408x720_i420.yuv");
        mRenderer.setYuvData(i420, 408, 720);
//        byte[] i420 = FileUtil.getAssertData(this, "204x360_i420.yuv");
//        mRenderer.setYuvData(i420, 204, 360);
        mGlSurfaceView.requestRender(); // 手动触发渲染
    }

    private boolean checkOpenGLES30() {
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x30000);
    }

    @Override
    protected void onPause() {
        mGlSurfaceView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mGlSurfaceView.onResume();
        super.onResume();
    }
}