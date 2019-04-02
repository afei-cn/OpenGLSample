package com.afei.particles.program;

import android.content.Context;
import android.opengl.GLES20;

import com.afei.particles.bean.ParticleShooter;

import javax.microedition.khronos.opengles.GL;

public class ParticleShaderProgram extends ShaderProgram {

    private final int uMatrixLocation;
    private final int uTimeLocation;
    private final int uTextureUnitLocation;

    private final int aPositionLocation;
    private final int aColorLocation;
    private final int aDirectionVectorLocation;
    private final int aStartTimeLocation;

    public ParticleShaderProgram(Context context) {
        super(context, "particle.vsh", "particle.fsh");
        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MATRIX);
        uTimeLocation = GLES20.glGetUniformLocation(mProgram, U_TIME);
        uTextureUnitLocation = GLES20.glGetUniformLocation(mProgram, U_TEXTURE_UNIT);
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_Position);
        aColorLocation = GLES20.glGetAttribLocation(mProgram, A_Color);
        aDirectionVectorLocation = GLES20.glGetAttribLocation(mProgram, A_DirectionVector);
        aStartTimeLocation = GLES20.glGetAttribLocation(mProgram, A_StartTime);
    }

    public void setUniforms(float[] matrix, float elapsedTime, int textureId) {
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        GLES20.glUniform1f(uTimeLocation, elapsedTime);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(uTextureUnitLocation, 0);
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }

    public int getColorLocation() {
        return aColorLocation;
    }

    public int getDirectionVectorLocation() {
        return aDirectionVectorLocation;
    }

    public int getStartTimeLocation() {
        return aStartTimeLocation;
    }
}
