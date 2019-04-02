package com.afei.particles.program;

import android.content.Context;
import android.opengl.GLES20;

import com.afei.particles.util.Geometry;

public class HeightMapProgram extends ShaderProgram {

    private final int uVectorToLightLocation;
    private final int uMVMatrixLocation;
    private final int uIT_MVMatrixLocation;
    private final int uMVPMatrixLocation;
    private final int uPointLightPositionsLocation;
    private final int uPointLightColorsLocation;

    private final int aPositionLocation;
    private final int aNormalLocation;

    public HeightMapProgram(Context context) {
        super(context, "heightmap.vsh", "heightmap.fsh");
        uVectorToLightLocation = GLES20.glGetUniformLocation(mProgram, U_VECTOR_TO_LIGHT);
        uMVMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MV_MATRIX);
        uIT_MVMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_IT_MV_MATRIX);
        uMVPMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MVP_MATRIX);
        uPointLightPositionsLocation = GLES20.glGetUniformLocation(mProgram, U_POINT_LIGHT_POSITIONS);
        uPointLightColorsLocation = GLES20.glGetUniformLocation(mProgram, U_POINT_LIGHT_COLORS);
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_Position);
        aNormalLocation = GLES20.glGetAttribLocation(mProgram, A_Normal);
    }

    public void setUniforms(float[] mvMatrix,
                            float[] it_mvMatrix,
                            float[] mvpMatrix,
                            float[] vectorToDirectionalLight,
                            float[] pointLightPositions,
                            float[] pointLightColors) {
        GLES20.glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        GLES20.glUniformMatrix4fv(uIT_MVMatrixLocation, 1, false, it_mvMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        GLES20.glUniform3fv(uVectorToLightLocation, 1, vectorToDirectionalLight, 0);
        GLES20.glUniform4fv(uPointLightPositionsLocation, 3, pointLightPositions, 0);
        GLES20.glUniform3fv(uPointLightColorsLocation, 3, pointLightColors, 0);
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }

    public int getNormalLocation() {
        return aNormalLocation;
    }
}
