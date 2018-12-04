#include "com_afei_openglsample_NativeRenderer.h"

#include <stdlib.h>
#include <android/asset_manager_jni.h>
#include <GLES3/gl3.h>
#include "LogUtils.h"
#include "ShaderUtils.h"

GLuint g_program;
AAssetManager *g_pAssetManager = NULL;

char *readAssetFile(const char *filename, AAssetManager *mgr);

JNIEXPORT void JNICALL Java_com_afei_openglsample_NativeRenderer_glInit
        (JNIEnv *env, jobject instance) {
    char *vertexShaderSource = readAssetFile("vertex.vsh", g_pAssetManager);
    char *fragmentShaderSource = readAssetFile("fragment.fsh", g_pAssetManager);
    g_program = CreateProgram(vertexShaderSource, fragmentShaderSource);
    if (g_program == GL_NONE) {
        LOGE("gl init failed!");
    }
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // RGBA (range: 0.0 ~ 1.0)
}

JNIEXPORT void JNICALL Java_com_afei_openglsample_NativeRenderer_glResize
        (JNIEnv *env, jobject instance, jint width, jint height) {
    glViewport(0, 0, width, height); // 设置视距窗口
}

JNIEXPORT void JNICALL Java_com_afei_openglsample_NativeRenderer_glDraw
        (JNIEnv *env, jobject instance) {
    GLint vertexCount = 3; // 3个顶点
    GLfloat vertices[] = {
            0.0f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f
    };
    glClear(GL_COLOR_BUFFER_BIT); // clear color buffer
    // 1. 选择使用的程序
    glUseProgram(g_program);
    // 2. 加载顶点数据
    glVertexAttribPointer(0, vertexCount, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(0);
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

char *readAssetFile(const char *filename, AAssetManager *mgr) {
    if (mgr == NULL) {
        LOGE("pAssetManager is null!");
        return NULL;
    }
    AAsset *pAsset = AAssetManager_open(mgr, filename, AASSET_MODE_UNKNOWN);
    off_t len = AAsset_getLength(pAsset);
    char *pBuffer = (char *) malloc(len + 1);
    pBuffer[len] = '\0';
    int numByte = AAsset_read(pAsset, pBuffer, len);
    LOGD("numByte: %d, len: %d", numByte, len);
    AAsset_close(pAsset);
    return pBuffer;
}