#version 300 es

layout(location = 0) in vec4 v_Position;
layout(location = 1) in vec2 v_TextureCoord;

out vec2 texture_coord;

void main() {
    gl_Position = v_Position;
    texture_coord = v_TextureCoord;
}