#version 300 es

layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec2 vTexCoor;

out vec2 v_texCoor;

void main() {
    gl_Position = vPosition;
    v_texCoor = vTexCoor;
}