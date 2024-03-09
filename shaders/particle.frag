#version 450

layout(location = 0) in vec3 fragPos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 uv;
layout(location = 3) in vec4 tangent;

layout(location = 4) in vec4 particlePosition;
layout(location = 5) in vec4 particleInitialWorldPosition;
layout(location = 6) in vec4 particleVelocity;
layout(location = 7) in vec4 particleScale;
layout(location = 8) in vec4 particleColor;
layout(location = 9) in float particleLifeTime;
layout(location = 10) in float particleSpawnTime;

layout(location = 0) out vec4 outColor;

layout(set = 1, binding = 0) uniform sampler2D texSampler;

void main() {
    vec4 textureColor = texture(texSampler, uv);

    outColor = vec4(textureColor.rgb * 4.0, textureColor.a * particleScale.x * 0.01);
}
