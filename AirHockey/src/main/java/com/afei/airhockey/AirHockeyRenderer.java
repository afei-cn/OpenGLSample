package com.afei.airhockey;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.afei.airhockey.bean.Mallet;
import com.afei.airhockey.bean.Puck;
import com.afei.airhockey.bean.Table;
import com.afei.airhockey.program.ColorShaderProgram;
import com.afei.airhockey.program.TextureShaderProgram;
import com.afei.airhockey.util.Geometry;
import com.afei.airhockey.util.TextureUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private final Context mContext;

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mViewProjectionMatrix = new float[16];
    private final float[] mInvertedViewProjectionMatrix = new float[16];
    private final float[] mModelViewProjectionMatrix = new float[16];

    private Table mTable;
    private Mallet mMallet;
    private Puck mPuck;

    private TextureShaderProgram mTextureProgram;
    private ColorShaderProgram mColorProgram;

    private int mTextureId;

    private boolean mMalletPressed = false;
    private Geometry.Point mBlueMalletPosition;
    private Geometry.Point mPreviousBlueMalletPosition;

    private final float leftBound = -0.5f;
    private final float rightBound = 0.5f;
    private final float farBound = -0.8f;
    private final float nearBound = 0.8f;

    private Geometry.Point mPuckPosition;
    private Geometry.Vector mPuckVector;

    public AirHockeyRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 0f);

        mTable = new Table();
        mMallet = new Mallet(0.08f, 0.15f, 32);
        mPuck = new Puck(0.06f, 0.02f, 32);

        mBlueMalletPosition = new Geometry.Point(0f, mMallet.height / 2f, 0.4f);
        mPuckPosition = new Geometry.Point(0f, mPuck.height / 2f, 0f);
        mPuckVector = new Geometry.Vector(0f, 0f, 0f);

        mTextureProgram = new TextureShaderProgram(mContext);
        mColorProgram = new ColorShaderProgram(mContext);

        mTextureId = TextureUtil.loadTexture(mContext, R.mipmap.air_hockey_surface);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        Matrix.perspectiveM(mProjectionMatrix, 0, 45, (float) width / (float) height, 1f, 10f);
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // clear the renderer surface
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Translate the puck by its vector
        mPuckPosition = mPuckPosition.translate(mPuckVector);
        if (mPuckPosition.x < leftBound + mPuck.radius || mPuckPosition.x > rightBound - mPuck.radius) {
            mPuckVector = new Geometry.Vector(-mPuckVector.x, mPuckVector.y, mPuckVector.z);
            mPuckVector.scale(0.9f);
        }
        if (mPuckPosition.z < farBound + mPuck.radius || mPuckPosition.z > nearBound - mPuck.radius) {
            mPuckVector = new Geometry.Vector(mPuckVector.x, mPuckVector.y, -mPuckVector.z);
            mPuckVector.scale(0.9f);
        }
        // Clamp the puck position.
        mPuckPosition = new Geometry.Point(
                clamp(mPuckPosition.x, leftBound + mPuck.radius, rightBound - mPuck.radius),
                mPuckPosition.y,
                clamp(mPuckPosition.z, farBound + mPuck.radius, nearBound - mPuck.radius)
        );
        mPuckVector.scale(0.99f);

        // Update the viewProjection matrix, and create an inverted matrix for
        // touch picking.
        Matrix.multiplyMM(mViewProjectionMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        Matrix.invertM(mInvertedViewProjectionMatrix, 0, mViewProjectionMatrix, 0);

        // draw table
        positionTableInScene();
        mTextureProgram.usrProgram();
        mTextureProgram.setUniforms(mModelViewProjectionMatrix, mTextureId);
        mTable.bindData(mTextureProgram);
        mTable.draw();

        // draw mallet
        positionObjectInScene(0f, mMallet.height / 2f, -0.4f);
        mColorProgram.usrProgram();
        mColorProgram.setUniforms(mModelViewProjectionMatrix, 1f, 0f, 0f);
        mMallet.bindData(mColorProgram);
        mMallet.draw();

        positionObjectInScene(mBlueMalletPosition.x, mBlueMalletPosition.y, mBlueMalletPosition.z);
        mColorProgram.setUniforms(mModelViewProjectionMatrix, 0f, 0f, 1f);
        mMallet.draw();

        // Draw the puck.
        positionObjectInScene(mPuckPosition.x, mPuckPosition.y, mPuckPosition.z);
        mColorProgram.setUniforms(mModelViewProjectionMatrix, 0.8f, 0.8f, 1f);
        mPuck.bindData(mColorProgram);
        mPuck.draw();
    }

    private void positionTableInScene() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, -90, 1f, 0f, 0f);
        Matrix.multiplyMM(mModelViewProjectionMatrix, 0, mViewProjectionMatrix, 0, mModelMatrix, 0);
    }

    private void positionObjectInScene(float x, float y, float z) {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, x, y, z);
        Matrix.multiplyMM(mModelViewProjectionMatrix, 0, mViewProjectionMatrix, 0, mModelMatrix, 0);
    }

    public void handleTouchPress(float normalizedX, float normalizedY) {

        Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);

        // Now test if this ray intersects with the mallet by creating a
        // bounding sphere that wraps the mallet.
        Geometry.Sphere malletBoundingSphere = new Geometry.Sphere(new Geometry.Point(
                mBlueMalletPosition.x,
                mBlueMalletPosition.y,
                mBlueMalletPosition.z),
                mMallet.height / 2f);

        // If the ray intersects (if the user touched a part of the screen that
        // intersects the mallet's bounding sphere), then set malletPressed =
        // true.
        mMalletPressed = Geometry.intersects(malletBoundingSphere, ray);
    }

    public void handleTouchDrag(float normalizedX, float normalizedY) {
        if (mMalletPressed) {
            Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
            Geometry.Plane plane = new Geometry.Plane(new Geometry.Point(0f, 0f, 0f), new Geometry.Vector(0, 1, 0));
            Geometry.Point touchedPoint = Geometry.intersectionPoint(ray, plane);

            mPreviousBlueMalletPosition = mBlueMalletPosition;
                mBlueMalletPosition = new Geometry.Point(
                    clamp(touchedPoint.x, leftBound + mMallet.radius, rightBound - mMallet.radius),
                    mMallet.height / 2f,
                    clamp(touchedPoint.z, 0f + mMallet.radius, nearBound - mMallet.radius));

            // Now test if mallet has struck the puck.
            float distance = Geometry.vectorBetween(mBlueMalletPosition, mPuckPosition).length();
            if (distance < (mPuck.radius + mMallet.radius)) {
                // The mallet has struck the puck. Now send the puck flying
                // based on the mallet velocity.
                mPuckVector = Geometry.vectorBetween(mPreviousBlueMalletPosition, mBlueMalletPosition);
            }
        }
    }

    private Geometry.Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {
        // We'll convert these normalized device coordinates into world-space
        // coordinates. We'll pick a point on the near and far planes, and draw a
        // line between them. To do this transform, we need to first multiply by
        // the inverse matrix, and then we need to undo the perspective divide.
        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
        final float[] farPointNdc =  {normalizedX, normalizedY,  1, 1};

        final float[] nearPointWorld = new float[4];
        final float[] farPointWorld = new float[4];

        Matrix.multiplyMV(nearPointWorld, 0, mInvertedViewProjectionMatrix, 0, nearPointNdc, 0);
        Matrix.multiplyMV(farPointWorld, 0, mInvertedViewProjectionMatrix, 0, farPointNdc, 0);

        // Why are we dividing by W? We multiplied our vector by an inverse
        // matrix, so the W value that we end up is actually the *inverse* of
        // what the projection matrix would create. By dividing all 3 components
        // by W, we effectively undo the hardware perspective divide.
        divideByW(nearPointWorld);
        divideByW(farPointWorld);

        // We don't care about the W value anymore, because our points are now
        // in world coordinates.
        Geometry.Point nearPointRay = new Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
        Geometry.Point farPointRay = new Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);

        return new Geometry.Ray(nearPointRay, Geometry.vectorBetween(nearPointRay, farPointRay));
    }

    private void divideByW(float[] vector) {
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }

    private float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }

}