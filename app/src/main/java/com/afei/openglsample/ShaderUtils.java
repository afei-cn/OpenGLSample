package com.afei.openglsample;

import android.content.res.Resources;
import android.opengl.GLES30;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class ShaderUtils {

    public static final String TAG = "ShaderUtils";

    public static int loadShader(int type, String source) {
        // 1. create shader
        int shader = GLES30.glCreateShader(type);
        if (shader == GLES30.GL_NONE) {
            Log.e(TAG, "create shared failed! type: " + type);
            return GLES30.GL_NONE;
        }
        // 2. load shader source
        GLES30.glShaderSource(shader, source);
        // 3. compile shared source
        GLES30.glCompileShader(shader);
        // 4. check compile status
        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == GLES30.GL_NONE) { // compile failed
            Log.e(TAG, "Error compiling shader. type: " + type + ":");
            Log.e(TAG, GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader); // delete shader
            shader = GLES30.GL_NONE;
        }
        return shader;
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        // 1. load shader
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == GLES30.GL_NONE) {
            Log.e(TAG, "load vertex shader failed! ");
            return GLES30.GL_NONE;
        }
        int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == GLES30.GL_NONE) {
            Log.e(TAG, "load fragment shader failed! ");
            return GLES30.GL_NONE;
        }
        // 2. create gl program
        int program = GLES30.glCreateProgram();
        if (program == GLES30.GL_NONE) {
            Log.e(TAG, "create program failed! ");
            return GLES30.GL_NONE;
        }
        // 3. attach shader
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        // we can delete shader after attach
        GLES30.glDeleteShader(vertexShader);
        GLES30.glDeleteShader(fragmentShader);
        // 4. link program
        GLES30.glLinkProgram(program);
        // 5. check link status
        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == GLES30.GL_NONE) { // link failed
            Log.e(TAG, "Error link program: ");
            Log.e(TAG, GLES30.glGetProgramInfoLog(program));
            GLES30.glDeleteProgram(program); // delete program
            return GLES30.GL_NONE;
        }
        return program;
    }

    public static String loadFromAssets(String fileName, Resources resources) {
        String result = null;
        try {
            InputStream is = resources.getAssets().open(fileName);
            int length = is.available();
            byte[] data = new byte[length];
            is.read(data);
            is.close();
            result = new String(data, "UTF-8");
            result.replace("\\r\\n", "\\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
