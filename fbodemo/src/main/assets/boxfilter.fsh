#version 300 es

precision mediump float;

layout (location = 2) uniform sampler2D s_texture;
layout (location = 3) uniform int u_kernelSize;
layout (location = 4) uniform int u_boxFilterType;  // boxFilter: 1, boxFilterHorizontal: 2, boxFilterVertical: 3

in vec2 v_texCoor;
out vec4 fragColor;

vec4 boxFilter() {
    if (u_kernelSize <= 1) {
        return texture(s_texture, v_texCoor);
    }
    ivec2 texSize = textureSize(s_texture, 0);
    float xStep = 1.0 / float(texSize.x);
    float yStep = 1.0 / float(texSize.y);
    vec4 sum = vec4(0.0);
    int num = 0;
    // 复杂度：N^2
    for (int i = -u_kernelSize; i <= u_kernelSize; i++) {
        for (int j = -u_kernelSize; j <= u_kernelSize; j++) {
            float x = v_texCoor.x + float(i) * xStep;
            float y = v_texCoor.y + float(j) * yStep;
            sum += texture(s_texture, vec2(x, y));
            num++;
        }
    }
    return sum / float(num);
}

vec4 boxFilterHorizontal() {
    if (u_kernelSize <= 1) {
        return texture(s_texture, v_texCoor);
    }
    ivec2 texSize = textureSize(s_texture, 0);
    float xStep = 1.0 / float(texSize.x);
    vec4 sum = vec4(0.0);
    int num = 0;
    // 复杂度：N
    for (int i = -u_kernelSize; i <= u_kernelSize; i++) {
        float x = v_texCoor.x + float(i) * xStep;
        sum += texture(s_texture, vec2(x, v_texCoor.y));
        num++;
    }
    return sum / float(num);
}

vec4 boxFilterVertical() {
    if (u_kernelSize <= 1) {
        return texture(s_texture, v_texCoor);
    }
    ivec2 texSize = textureSize(s_texture, 0);
    float yStep = 1.0 / float(texSize.y);
    vec4 sum = vec4(0.0);
    int num = 0;
    // 复杂度：N
    for (int i = -u_kernelSize; i <= u_kernelSize; i++) {
        float y = v_texCoor.y + float(i) * yStep;
        if (y < 0.0 || y > 1.0) {
            continue;
        }
        sum += texture(s_texture, vec2(v_texCoor.x, y));
        num++;
    }
    return sum / float(num);
}

void main() {
    if (u_boxFilterType == 1) {
        fragColor = boxFilter();
    } else if (u_boxFilterType == 2) {
        fragColor = boxFilterHorizontal();
    } else if (u_boxFilterType == 3){
        fragColor = boxFilterVertical();
    } else {
        fragColor = texture(s_texture, v_texCoor); // origin
    }
}