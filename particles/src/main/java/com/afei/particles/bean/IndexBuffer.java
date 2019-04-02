package com.afei.particles.bean;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class IndexBuffer {

    private final int ID;

    public IndexBuffer(short[] indexData) {
        final int buffers[] = new int[1];
        GLES20.glGenBuffers(buffers.length, buffers, 0);
        if (buffers[0] == 0) {
            throw new RuntimeException("Could not create a new vertex buffer object");
        }
        ID = buffers[0];

        // bind buffer
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ID);

        // transfer data to native
        ShortBuffer indexArray = ByteBuffer.allocateDirect(indexData.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indexData);
        indexArray.position(0);

        // transfer native to gpu
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexArray.capacity() * 2, indexArray, GLES20.GL_STATIC_DRAW);

        // unbind
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public int getBufferId() {
        return ID;
    }
}
