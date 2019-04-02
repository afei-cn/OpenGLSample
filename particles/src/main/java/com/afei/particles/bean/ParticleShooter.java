package com.afei.particles.bean;

import android.opengl.Matrix;

import com.afei.particles.util.Geometry;

import java.util.Random;

public class ParticleShooter {

    private final Geometry.Point mPosition;
    private final int mColor;

    private final float mAngle;
    private final float mSpeed;

    private final Random mRandom = new Random();

    private float[] mRotateMatrix = new float[16];
    private float[] mDirectionVector = new float[4];
    private float[] mResultVector = new float[4];

    public ParticleShooter(Geometry.Point position, Geometry.Vector direction, int color, float angle, float speed) {
        mPosition = position;
        mColor = color;
        mAngle = angle;
        mSpeed = speed;

        mDirectionVector[0] = direction.x;
        mDirectionVector[1] = direction.y;
        mDirectionVector[2] = direction.z;
    }

    public void addParticles(ParticleSystem system, float currentTime, int count) {
        for (int i = 0; i < count; i++) {
            Matrix.setRotateEulerM(mRotateMatrix, 0,
                    (mRandom.nextFloat() - 0.5f) * mAngle,
                    (mRandom.nextFloat() - 0.5f) * mAngle,
                    (mRandom.nextFloat() - 0.5f) * mAngle);
            Matrix.multiplyMV(mResultVector, 0, mRotateMatrix, 0, mDirectionVector, 0);
            float speedAdjustment = 1f + mRandom.nextFloat() * mSpeed;
            Geometry.Vector thisDirection = new Geometry.Vector(
                    mResultVector[0] * speedAdjustment,
                    mResultVector[1] * speedAdjustment,
                    mResultVector[2] * speedAdjustment);
            system.addParticle(mPosition, mColor, thisDirection, currentTime);
        }
    }
}
