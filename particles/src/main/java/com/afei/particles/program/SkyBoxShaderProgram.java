package com.afei.particles.program;

import android.content.Context;
import android.opengl.GLES20;

public class SkyBoxShaderProgram extends ShaderProgram {

    private final int uMatrixLocation;
    private final int uTextureUnitLocation;

    private final int aPositionLocation;

    public SkyBoxShaderProgram(Context context) {
        super(context, "skybox.vsh", "skybox.fsh");
        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MATRIX);
        uTextureUnitLocation = GLES20.glGetUniformLocation(mProgram, U_TEXTURE_UNIT);
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_Position);
    }

    public void setUniforms(float[] matrix, int textureId) {
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
        GLES20.glUniform1i(uTextureUnitLocation, 0);
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }

}
