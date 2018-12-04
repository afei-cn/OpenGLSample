#ifndef _SHADER_UTILS_H_
#define _SHADER_UTILS_H_

#include <GLES3/gl3.h>

GLuint LoadShader(GLenum type, const char *shaderSource);
GLuint CreateProgram(const char* vertexSource, const char* fragmentSource);

#endif //_SHADER_UTILS_H_
