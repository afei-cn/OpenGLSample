package com.afei.particles.bean;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VertexBuffer {

    private final int ID;

    public VertexBuffer(float[] vertexData) {
        final int buffers[] = new int[1];
        GLES20.glGenBuffers(buffers.length, buffers, 0);
        if (buffers[0] == 0) {
            throw new RuntimeException("Could not create a new vertex buffer object");
        }
        ID = buffers[0];

        // bind buffer
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, ID);

        // transfer data to native
        FloatBuffer vertexArray = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexArray.position(0);

        // transfer native to gpu
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexArray.capacity() * 4, vertexArray, GLES20.GL_STATIC_DRAW);

        // unbind
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void setVertexAttribPointer(int offset, int location, int size, int stride) {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, ID);
        GLES20.glVertexAttribPointer(location, size, GLES20.GL_FLOAT, false, stride, offset);
        GLES20.glEnableVertexAttribArray(location);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

}
