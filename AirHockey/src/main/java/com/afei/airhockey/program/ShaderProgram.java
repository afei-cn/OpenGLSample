package com.afei.airhockey.program;

import android.content.Context;
import android.opengl.GLES20;

import com.afei.airhockey.util.ShaderUtil;

public class ShaderProgram {

    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_COLOR = "u_Color";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";

    protected static final String A_Position = "a_Position";
    protected static final String A_Texture_Coordinates = "a_TextureCoordinates";

    protected final int mProgram;

    protected ShaderProgram(Context context, String vertexFileName, String fragmentFileName) {
        String vertexSource = ShaderUtil.loadFromAssets(vertexFileName, context.getResources());
        String fragmentSource = ShaderUtil.loadFromAssets(fragmentFileName, context.getResources());
        mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
    }

    public void usrProgram() {
        GLES20.glUseProgram(mProgram);
    }

}
