package com.afei.particles.bean;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VertexArray {

    private final FloatBuffer mFloatBuffer;

    public VertexArray(float[] vertexData) {
        mFloatBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
    }

    public void setVertexAttribPointer(int offset, int location, int size, int stride) {
        mFloatBuffer.position(offset);
        GLES20.glVertexAttribPointer(location, size, GLES20.GL_FLOAT, false, stride, mFloatBuffer);
        GLES20.glEnableVertexAttribArray(location);
        mFloatBuffer.position(0);
    }

    public void updateBuffer(float[] data, int start, int count) {
        mFloatBuffer.position(start);
        mFloatBuffer.put(data, start, count);
        mFloatBuffer.position(0);
    }

}
