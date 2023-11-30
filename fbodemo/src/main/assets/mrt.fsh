#version 300 es

precision mediump float;

layout (location = 2) uniform sampler2D s_texture;

in vec2 v_texCoor;
out vec4 fragColor1;  // 输出到第一个颜色附着点
out vec4 fragColor2;  // 输出到第二个颜色附着点
out vec4 fragColor3;  // 输出到第三个颜色附着点

void main() {
    vec4 color = texture(s_texture, v_texCoor); // origin
    fragColor1 = vec4(1.0) - color; // inverted
    fragColor2 = mix(color, vec4(1.0, 0.0, 0.0, 1.0), 0.5); // mix with color red
    fragColor3 = mix(color, vec4(0.0, 0.0, 1.0, 1.0), 0.5); // mix with color blur
}