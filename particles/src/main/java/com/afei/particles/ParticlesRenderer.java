package com.afei.particles;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.afei.particles.bean.HeightMap;
import com.afei.particles.bean.ParticleShooter;
import com.afei.particles.bean.ParticleSystem;
import com.afei.particles.bean.SkyBox;
import com.afei.particles.program.HeightMapProgram;
import com.afei.particles.program.ParticleShaderProgram;
import com.afei.particles.program.SkyBoxShaderProgram;
import com.afei.particles.util.Geometry;
import com.afei.particles.util.TextureUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ParticlesRenderer implements GLSurfaceView.Renderer {

    private final Context mContext;

    private final float[] mModelMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mViewMatrixForSkyBox = new float[16];
    private final float[] mProjectionMatrix = new float[16];

    private final float[] mTempMatrix = new float[16];
    private final float[] mModelViewProjectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    private final float[] it_modelViewMatrix = new float[16];

    final float[] vectorToLight = {0.30f, 0.35f, -0.89f, 0f};

    private final float[] pointLightPositions = new float[]{
            -1f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            1f, 1f, 0f, 1f};

    private final float[] pointLightColors = new float[]{
            1.00f, 0.20f, 0.02f,
            0.02f, 0.25f, 0.02f,
            0.02f, 0.20f, 1.00f};

    private ParticleShaderProgram mParticleShaderProgram;
    private ParticleSystem mParticleSystem;
    private ParticleShooter mRedShooter;
    private ParticleShooter mGreenShooter;
    private ParticleShooter mBlueShooter;
    private int mParticleTextureId;

    private SkyBoxShaderProgram mSkyBoxShaderProgram;
    private SkyBox mSkyBox;
    private int mSkyBoxTextureId;

    private HeightMapProgram mHeightMapProgram;
    private HeightMap mHeightMap;

    private long mStartTime;
    private float mRotationX;
    private float mRotationY;

    private long mFrameStartTime;
    private float mOffsetX;
    private float mOffsetY;

    public ParticlesRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // height map
        mHeightMapProgram = new HeightMapProgram(mContext);
        mHeightMap = new HeightMap(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.heightmap));

        // sky box
        mSkyBoxShaderProgram = new SkyBoxShaderProgram(mContext);
        mSkyBox = new SkyBox();
        mSkyBoxTextureId = TextureUtil.loadCubeMap(mContext, new int[]{
                R.mipmap.night_left, R.mipmap.night_right, R.mipmap.night_bottom,
                R.mipmap.night_top, R.mipmap.night_front, R.mipmap.night_back
        });

        // particles
        mParticleShaderProgram = new ParticleShaderProgram(mContext);
        mParticleSystem = new ParticleSystem(10000);
        mStartTime = System.nanoTime();
        final Geometry.Vector particleDirection = new Geometry.Vector(0f, 0.5f, 0f);
        final float angle = 5f;
        final float speed = 1f;
        mRedShooter = new ParticleShooter(
                new Geometry.Point(-1f, 0f, 0f),
                particleDirection,
                Color.rgb(255, 50, 5),
                angle,
                speed);
        mGreenShooter = new ParticleShooter(
                new Geometry.Point(0f, 0f, 0f),
                particleDirection,
                Color.rgb(25, 255, 25),
                angle,
                speed);
        mBlueShooter = new ParticleShooter(
                new Geometry.Point(1f, 0f, 0f),
                particleDirection,
                Color.rgb(5, 50, 255),
                angle,
                speed);
        mParticleTextureId = TextureUtil.loadTexture(mContext, R.mipmap.particle_texture);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.perspectiveM(mProjectionMatrix, 0, 45, (float) width / (float) height, 1f, 100f);
        updateViewMatrices();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        limitFrameRate(24);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        drawHeightMap();
        drawSkyBox();
        drawParticles();
    }

    private void limitFrameRate(int framesPerSecond) {
        long elapsedFrameTimeMs = SystemClock.elapsedRealtime() - mFrameStartTime;
        long expectedFrameTimeMs = 1000 / framesPerSecond;
        long timeToSleepMs = expectedFrameTimeMs - elapsedFrameTimeMs;

        if (timeToSleepMs > 0) {
            SystemClock.sleep(timeToSleepMs);
        }
        mFrameStartTime = SystemClock.elapsedRealtime();
    }

    private void drawHeightMap() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix, 0, 100f, 10f, 100f);
        updateMvpMatrix();

        mHeightMapProgram.usrProgram();

        final float[] vectorToLightInEyeSpace = new float[4];
        final float[] pointPositionsInEyeSpace = new float[12];
        Matrix.multiplyMV(vectorToLightInEyeSpace, 0, mViewMatrix, 0, vectorToLight, 0);
        Matrix.multiplyMV(pointPositionsInEyeSpace, 0, mViewMatrix, 0, pointLightPositions, 0);
        Matrix.multiplyMV(pointPositionsInEyeSpace, 4, mViewMatrix, 0, pointLightPositions, 4);
        Matrix.multiplyMV(pointPositionsInEyeSpace, 8, mViewMatrix, 0, pointLightPositions, 8);
        mHeightMapProgram.setUniforms(modelViewMatrix,
                it_modelViewMatrix,
                mModelViewProjectionMatrix,
                vectorToLightInEyeSpace,
                pointPositionsInEyeSpace,
                pointLightColors);

        mHeightMap.bindData(mHeightMapProgram);
        mHeightMap.draw();
    }

    private void drawSkyBox() {
        Matrix.setIdentityM(mModelMatrix, 0);
        updateMvpMatrixForSkyBox();

        GLES20.glDepthFunc(GLES20.GL_LEQUAL); // This avoids problems with the skybox itself getting clipped.
        mSkyBoxShaderProgram.usrProgram();
        mSkyBoxShaderProgram.setUniforms(mModelViewProjectionMatrix, mSkyBoxTextureId);
        mSkyBox.bindData(mSkyBoxShaderProgram);
        mSkyBox.draw();
        GLES20.glDepthFunc(GLES20.GL_LESS);
    }

    private void drawParticles() {
        float currentTime = (System.nanoTime() - mStartTime) / 1000000000f;

        mRedShooter.addParticles(mParticleSystem, currentTime, 1);
        mGreenShooter.addParticles(mParticleSystem, currentTime, 1);
        mBlueShooter.addParticles(mParticleSystem, currentTime, 1);

        Matrix.setIdentityM(mModelMatrix, 0);
        updateMvpMatrix();

        GLES20.glDepthMask(false);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        mParticleShaderProgram.usrProgram();
        mParticleShaderProgram.setUniforms(mModelViewProjectionMatrix, currentTime, mParticleTextureId);
        mParticleSystem.bindData(mParticleShaderProgram);
        mParticleSystem.draw();

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDepthMask(true);
    }

    public void handleDrag(float deltaX, float deltaY) {
        mRotationX += deltaX / 16f;
        mRotationY += deltaY / 16f;

        if (mRotationY < -90) {
            mRotationY = -90;
        } else if (mRotationY > 90) {
            mRotationY = 90;
        }
        updateViewMatrices();
    }

    public void handleOffsetsChanged(float offsetX, float offsetY) {
        mOffsetX = (offsetX - 0.5f) * 2.5f;
        mOffsetY = (offsetY - 0.5f) * 2.5f;
        updateViewMatrices();
    }

    private void updateViewMatrices() {
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.rotateM(mViewMatrix, 0, -mRotationY, 1f, 0f, 0f);
        Matrix.rotateM(mViewMatrix, 0, -mRotationX, 0f, 1f, 0f);
        System.arraycopy(mViewMatrix, 0, mViewMatrixForSkyBox, 0, mViewMatrix.length);
        Matrix.translateM(mViewMatrix, 0, -mOffsetX, -1.5f - mOffsetY, -5f);
    }

    private void updateMvpMatrix() {
        Matrix.multiplyMM(modelViewMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.invertM(mTempMatrix, 0, modelViewMatrix, 0);
        Matrix.transposeM(it_modelViewMatrix, 0, mTempMatrix, 0);
        Matrix.multiplyMM(mModelViewProjectionMatrix, 0, mProjectionMatrix, 0, modelViewMatrix, 0);
    }

    private void updateMvpMatrixForSkyBox() {
        Matrix.multiplyMM(mTempMatrix, 0, mViewMatrixForSkyBox, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mModelViewProjectionMatrix, 0, mProjectionMatrix, 0, mTempMatrix, 0);
    }

}
