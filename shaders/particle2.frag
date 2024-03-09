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

    if (particleSpawnTime > 0.0) {
        outColor = vec4(0.0, 0.0, 0.0, 0.0);
        return;
    }
    if (particleLifeTime <= 0.0) {
        outColor = vec4(0.0, 0.0, 0.0, 0.0);
        return;
    }

    // particles fade out over their lifetime, combined with the texture color
    float alpha = particleLifeTime / 1.0;

    vec4 diffuseColor = vec4(textureColor.rgb, (textureColor.a * alpha));

    // mix the texture color with the particle color
    outColor = diffuseColor * particleColor;
}
