#include "com_afei_texturedemo_NativeRenderer.h"

#include <android/asset_manager_jni.h>
#include <GLES3/gl3.h>
#include "LogUtils.h"
#include "ShaderUtils.h"
#include "TextureUtils.h"

GLuint g_program;
AAssetManager *g_pAssetManager = NULL;

JNIEXPORT void JNICALL Java_com_afei_texturedemo_NativeRenderer_glInit
        (JNIEnv *env, jobject instance) {
    char *vertexShaderSource = readAssetFile("vertex.vsh", g_pAssetManager);
    char *fragmentShaderSource = readAssetFile("fragment.fsh", g_pAssetManager);
    g_program = CreateProgram(vertexShaderSource, fragmentShaderSource);
    if (g_program == GL_NONE) {
        LOGE("gl init failed!");
    }
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // 背景颜色设置为黑色 RGBA (range: 0.0 ~ 1.0)
}

JNIEXPORT void JNICALL Java_com_afei_texturedemo_NativeRenderer_glResize
        (JNIEnv *env, jobject instance, jint width, jint height) {
    glViewport(0, 0, width, height); // 设置视距窗口
}

JNIEXPORT void JNICALL Java_com_afei_texturedemo_NativeRenderer_glDraw
        (JNIEnv *env, jobject instance) {
    glClear(GL_COLOR_BUFFER_BIT); // clear color buffer
    // OpenGL的世界坐标系是 [-1, -1, 1, 1]，纹理的坐标系为 [0, 0, 1, 1]
    GLfloat vertices[] = {
            // 前三个数字为顶点坐标(x, y, z)，后两个数字为纹理坐标(s, t)
            // 第一个三角形
            1.0,  1.0,  0.0,     1.0, 0.0,
            1.0,  -1.0, 0.0,     1.0, 1.0,
            -1.0, -1.0, 0.0,     0.0, 1.0,
            // 第二个三角形
            1.0,  1.0,  0.0,     1.0, 0.0,
            -1.0, -1.0, 0.0,     0.0, 1.0,
            -1.0, 1.0,  0.0,     0.0, 0.0
    };

    // 1. 选择使用的程序
    glUseProgram(g_program);
    // 2. 加载纹理
    GLuint textureId = loadTexture();
    glActiveTexture(GL_TEXTURE0); // 激活TEXTURE0
    glBindTexture(GL_TEXTURE_2D, textureId);
    GLint location = glGetUniformLocation(g_program, "s_texture");
    glUniform1i(location, 0); // 因为激活的是TEXTURE0，所以要给这个纹理赋值0
    // 3. 加载顶点数据
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 5 * sizeof(float), vertices);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 5 * sizeof(float), vertices + 3);
    glEnableVertexAttribArray(1);
    // 4. 绘制
    glDrawArrays(GL_TRIANGLES, 0, 6);
}

JNIEXPORT void JNICALL Java_com_afei_texturedemo_NativeRenderer_registerAssetManager
        (JNIEnv *env, jobject instance, jobject assetManager) {
    if (assetManager) {
        g_pAssetManager = AAssetManager_fromJava(env, assetManager);
    } else {
        LOGE("assetManager is null!")
    }
}

