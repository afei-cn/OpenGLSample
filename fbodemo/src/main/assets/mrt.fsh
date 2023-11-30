#version 300 es

precision mediump float;

layout (location = 2) uniform sampler2D s_texture;

in vec2 v_texCoor;
out vec4 fragColor1;  // 输出到第一个颜色附着点
out vec4 fragColor2;  // 输出到第二个颜色附着点

void main() {
    fragColor1 = texture(s_texture, v_texCoor); // origin
    fragColor2 = vec4(1.0) - fragColor1; // inverted
}