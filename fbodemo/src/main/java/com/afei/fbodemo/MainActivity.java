package com.afei.fbodemo;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private AutoFitGLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        findViewById(R.id.test1_btn).setOnClickListener(this::onClick);
        findViewById(R.id.test2_btn).setOnClickListener(this::onClick);
        findViewById(R.id.test3_btn).setOnClickListener(this::onClick);
        findViewById(R.id.test4_btn).setOnClickListener(this::onClick);
        findViewById(R.id.test5_btn).setOnClickListener(this::onClick);
        findViewById(R.id.test6_btn).setOnClickListener(this::onClick);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.test1_btn:
                mGLSurfaceView.drawBitmap(false);
                break;
            case R.id.test2_btn:
                mGLSurfaceView.drawBitmap(true);
                break;
            case R.id.test3_btn:
                mGLSurfaceView.drawBitmapUseFBO();
                break;
            case R.id.test4_btn:
                mGLSurfaceView.drawBitmapMRT(1);
                break;
            case R.id.test5_btn:
                mGLSurfaceView.drawBitmapMRT(2);
                break;
            case R.id.test6_btn:
                mGLSurfaceView.drawBitmapMRT(3);
                break;
            default:
                break;
        }
    }

}