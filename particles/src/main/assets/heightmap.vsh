uniform mat4 u_MVMatrix; // 模型矩阵
uniform mat4 u_IT_MVMatrix; // 倒置模型矩阵
uniform mat4 u_MVPMatrix; // 模型视图投影矩阵

uniform vec3 u_VectorToLight; // In eye space
uniform vec4 u_PointLightPositions[3]; // In eye space
uniform vec3 u_PointLightColors[3];

attribute vec4 a_Position;
attribute vec3 a_Normal;

varying vec3 v_Color;

vec3 materialColor;
vec4 eyeSpacePosition;
vec3 eyeSpaceNormal;

vec3 getAmbientLighting();
vec3 getDirectionalLighting();
vec3 getPointLighting();

void main() {
    materialColor = mix(vec3(0.180, 0.467, 0.153), // dark green
                        vec3(0.660, 0.670, 0.680), // stony gray
                        a_Position.y);
    eyeSpacePosition = u_MVMatrix * a_Position;

    // The model normals need to be adjusted as per the transpose
    // of the inverse of the model view matrix.
    eyeSpaceNormal = normalize(vec3(u_IT_MVMatrix * vec4(a_Normal, 0.0)));

    v_Color = getAmbientLighting();
    v_Color += getDirectionalLighting();
    v_Color += getPointLighting();

    gl_Position = u_MVPMatrix * a_Position;
}

vec3 getAmbientLighting() {
    return materialColor * 0.1;
}

vec3 getDirectionalLighting() {
    return materialColor * 0.3 * max(dot(eyeSpaceNormal, u_VectorToLight), 0.0);
}

vec3 getPointLighting() {
    vec3 lightingSum = vec3(0.0);

    for (int i = 0; i < 3; i++) {
        vec3 toPointLight = vec3(u_PointLightPositions[i]) - vec3(eyeSpacePosition);
        float distance = length(toPointLight);
        toPointLight = normalize(toPointLight);

        float cosine = max(dot(eyeSpaceNormal, toPointLight), 0.0);
        lightingSum += (materialColor * u_PointLightColors[i] * 5.0 * cosine) / distance;
    }

    return lightingSum;
}