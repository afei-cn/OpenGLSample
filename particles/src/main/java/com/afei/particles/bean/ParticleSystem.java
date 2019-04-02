package com.afei.particles.bean;

import android.graphics.Color;
import android.opengl.GLES20;

import com.afei.particles.program.ParticleShaderProgram;
import com.afei.particles.util.Geometry;

public class ParticleSystem {

    private static final int BYTES_PRE_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int VECTOR_COMPONENT_COUNT = 3;
    private static final int START_TIME_COMPONENT_COUNT = 1;
    private static final int TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT
            + COLOR_COMPONENT_COUNT
            + VECTOR_COMPONENT_COUNT
            + START_TIME_COMPONENT_COUNT;
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PRE_FLOAT;

    private final float[] PARTICLES;
    private final VertexArray VERTEX_DATA;
    private final int MAX_COUNT;

    private int mCurrentCount;
    private int mNextParticle;

    public ParticleSystem(int maxCount) {
        PARTICLES = new float[maxCount * TOTAL_COMPONENT_COUNT];
        VERTEX_DATA = new VertexArray(PARTICLES);
        MAX_COUNT = maxCount;
    }

    public void addParticle(Geometry.Point position, int color, Geometry.Vector direction, float startTime) {
        final int particleOffset = mNextParticle * TOTAL_COMPONENT_COUNT;
        int offset = particleOffset;
        mNextParticle++;

        if (mCurrentCount < MAX_COUNT) {
            mCurrentCount++;
        }
        if (mNextParticle == MAX_COUNT) {
            mNextParticle = 0;
        }

        PARTICLES[offset++] = position.x;
        PARTICLES[offset++] = position.y;
        PARTICLES[offset++] = position.z;

        PARTICLES[offset++] = Color.red(color) / 255f;
        PARTICLES[offset++] = Color.green(color) / 255f;
        PARTICLES[offset++] = Color.blue(color) / 255f;

        PARTICLES[offset++] = direction.x;
        PARTICLES[offset++] = direction.y;
        PARTICLES[offset++] = direction.z;

        PARTICLES[offset++] = startTime;

        VERTEX_DATA.updateBuffer(PARTICLES, particleOffset, TOTAL_COMPONENT_COUNT);
    }

    public void bindData(ParticleShaderProgram program) {
        int dataOffset = 0;
        VERTEX_DATA.setVertexAttribPointer(dataOffset, program.getPositionLocation(), POSITION_COMPONENT_COUNT, STRIDE);
        dataOffset += POSITION_COMPONENT_COUNT;
        VERTEX_DATA.setVertexAttribPointer(dataOffset, program.getColorLocation(), COLOR_COMPONENT_COUNT, STRIDE);
        dataOffset += COLOR_COMPONENT_COUNT;
        VERTEX_DATA.setVertexAttribPointer(dataOffset, program.getDirectionVectorLocation(), VECTOR_COMPONENT_COUNT, STRIDE);
        dataOffset += VECTOR_COMPONENT_COUNT;
        VERTEX_DATA.setVertexAttribPointer(dataOffset, program.getStartTimeLocation(), START_TIME_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mCurrentCount);
    }

}
