package com.afei.airhockey.program;

import android.content.Context;
import android.opengl.GLES20;

public class ColorShaderProgram extends ShaderProgram {

    private final int uMatrixLocation;
    private final int uColorLocation;

    private final int aPositionLocation;

    public ColorShaderProgram(Context context) {
        super(context, "color.vsh", "color.fsh");
        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MATRIX);
        uColorLocation = GLES20.glGetUniformLocation(mProgram, U_COLOR);
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_Position);
    }

    public void setUniforms(float[] matrix, float r, float g, float b) {
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        GLES20.glUniform4f(uColorLocation, r, g, b, 1f);
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }

    public int getColorLocation() {
        return uColorLocation;
    }
}
