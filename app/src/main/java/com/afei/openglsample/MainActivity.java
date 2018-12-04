package com.afei.openglsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.native_renderer_btn).setOnClickListener(this);
        findViewById(R.id.java_renderer_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, SampleActivity.class);
        switch (v.getId()) {
            case R.id.native_renderer_btn:
                intent.putExtra(SampleActivity.TYPE_NAME, SampleActivity.TYPE_NATIVE);
                break;
            case R.id.java_renderer_btn:
                intent.putExtra(SampleActivity.TYPE_NAME, SampleActivity.TYPE_JAVA);
                break;
        }
        startActivity(intent);
    }
}
