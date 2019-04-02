package com.afei.particles.program;

import android.content.Context;
import android.opengl.GLES20;

import com.afei.particles.util.ShaderUtil;

public class ShaderProgram {

    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TIME = "u_Time";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    protected static final String U_VECTOR_TO_LIGHT = "u_VectorToLight";
    protected static final String U_MV_MATRIX = "u_MVMatrix";
    protected static final String U_IT_MV_MATRIX = "u_IT_MVMatrix";
    protected static final String U_MVP_MATRIX = "u_MVPMatrix";
    protected static final String U_POINT_LIGHT_POSITIONS = "u_PointLightPositions";
    protected static final String U_POINT_LIGHT_COLORS = "u_PointLightColors";


    protected static final String A_Position = "a_Position";
    protected static final String A_Color = "a_Color";
    protected static final String A_DirectionVector = "a_DirectionVector";
    protected static final String A_Normal = "a_Normal";
    protected static final String A_StartTime = "a_StartTime";

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
