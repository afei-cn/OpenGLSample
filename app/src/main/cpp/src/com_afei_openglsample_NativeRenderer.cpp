#include "com_afei_openglsample_NativeRenderer.h"

#include <stdlib.h>
#include <android/asset_manager_jni.h>
#include <GLES3/gl3.h>
#include "LogUtils.h"
#include "ShaderUtils.h"

GLuint g_program;
GLint g_position_handle;
AAssetManager *g_pAssetManager = NULL;

JNIEXPORT void JNICALL Java_com_afei_openglsample_NativeRenderer_glInit
        (JNIEnv *env, jobject instance) {
    char *vertexShaderSource = readAssetFile("vertex.vsh", g_pAssetManager);
    char *fragmentShaderSource = readAssetFile("fragment.fsh", g_pAssetManager);
    g_program = CreateProgram(vertexShaderSource, fragmentShaderSource);
    if (g_program == GL_NONE) {
        LOGE("gl init failed!");
    }
    // vPosition 是在 'vertex.vsh' 文件中定义的
    GLint g_position_handle =glGetAttribLocation(g_program, "vPosition");
    LOGD("g_position_handle: %d", g_position_handle);
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // 背景颜色设置为黑色 RGBA (range: 0.0 ~ 1.0)
}

JNIEXPORT void JNICALL Java_com_afei_openglsample_NativeRenderer_glResize
        (JNIEnv *env, jobject instance, jint width, jint height) {
    glViewport(0, 0, width, height); // 设置视距窗口
}

JNIEXPORT void JNICALL Java_com_afei_openglsample_NativeRenderer_glDraw
        (JNIEnv *env, jobject instance) {
    GLint vertexCount = 3;
    // OpenGL的世界坐标系是 [-1, -1, 1, 1]
    GLfloat vertices[] = {
            0.0f, 0.5f, 0.0f, // 第一个点（x, y, z）
            -0.5f, -0.5f, 0.0f, // 第二个点（x, y, z）
            0.5f, -0.5f, 0.0f // 第三个点（x, y, z）
    };
    glClear(GL_COLOR_BUFFER_BIT); // clear color buffer
    // 1. 选择使用的程序
    glUseProgram(g_program);
    // 2. 加载顶点数据
    glVertexAttribPointer(g_position_handle, vertexCount, GL_FLOAT, GL_FALSE, 3 * 4, vertices);
    glEnableVertexAttribArray(g_position_handle);
    // 3. 绘制
    glDrawArrays(GL_TRIANGLES, 0, vertexCount);
}

JNIEXPORT void JNICALL Java_com_afei_openglsample_NativeRenderer_registerAssetManager
        (JNIEnv *env, jobject instance, jobject assetManager) {
    if (assetManager) {
        g_pAssetManager = AAssetManager_fromJava(env, assetManager);
    } else {
        LOGE("assetManager is null!")
    }
}

