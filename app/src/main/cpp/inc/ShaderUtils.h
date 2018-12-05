#ifndef _SHADER_UTILS_H_
#define _SHADER_UTILS_H_

#include <GLES3/gl3.h>
#include <android/asset_manager_jni.h>

GLuint LoadShader(GLenum type, const char *shaderSource);
GLuint CreateProgram(const char* vertexSource, const char* fragmentSource);
char *readAssetFile(const char *filename, AAssetManager *mgr);

#endif //_SHADER_UTILS_H_
