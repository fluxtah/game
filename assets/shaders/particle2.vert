#version 450

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec2 inUV;
layout(location = 3) in vec4 inTangent;

layout(location = 4) in vec4 inParticlePosition;
layout(location = 5) in vec4 inParticleInitialWorldPosition;
layout(location = 6) in vec4 inParticleVelocity;
layout(location = 7) in vec4 inParticleScale;
layout(location = 8) in vec4 inParticleColor;
layout(location = 9) in float inParticleLifeTime;
layout(location = 10) in float inParticleSpawnTime;

layout(location = 0) out vec3 fragPos;
layout(location = 1) out vec3 normal;
layout(location = 2) out vec2 uv;
layout(location = 3) out vec4 tangent;

layout(location = 4) out vec4 particlePosition;
layout(location = 5) out vec4 particleInitialWorldPosition;
layout(location = 6) out vec4 particleVelocity;
layout(location = 7) out vec4 particleScale;
layout(location = 8) out vec4 particleColor;
layout(location = 9) out float particleLifeTime;
layout(location = 10) out float particleSpawnTime;

layout(set = 0, binding = 0) uniform TransformUBO {
    mat4 model;
    mat4 view;
    mat4 proj;
} ubo;

void main() {
    if(inParticleSpawnTime >= 0.0 || inParticleLifeTime <= 0.0) {
        gl_Position = vec4(0.0, 0.0, 0.0, 1.0); // Set w to 1.0
        gl_Position.z = 2000.0; // Move it outside the normalized device coordinates, which range from -1 to 1
        return;
    }

    mat4 view = ubo.view;
    vec3 right = vec3(view[0][0], view[1][0], view[2][0]);
    vec3 up = vec3(view[0][1], view[1][1], view[2][1]);

    mat4 billboardMatrix = mat4(
    vec4(right, 0.0),
    vec4(up, 0.0),
    vec4(cross(up, right), 0.0),
    vec4(0.0, 0.0, 0.0, 1.0)
    );

    // Apply billboarding transformation to the particle's local position offsets (inPos)
    vec4 localBillboardedPos = billboardMatrix * vec4((inPos) * inParticleScale.xyz, 1.0);

    // Combine the particle's current world position (updated by the compute shader) with the local billboarding offset
    vec4 worldPos = inParticlePosition + localBillboardedPos + inParticleInitialWorldPosition;

    fragPos = vec3(worldPos); // Position in world space
    normal = mat3(transpose(inverse(ubo.model))) * inNormal;
    uv = inUV;
    tangent = inTangent;

    particlePosition = inParticlePosition;
    particleInitialWorldPosition = inParticleInitialWorldPosition;
    particleVelocity = inParticleVelocity;
    particleScale = inParticleScale;
    particleColor = inParticleColor;
    particleLifeTime = inParticleLifeTime;
    particleSpawnTime = inParticleSpawnTime;

    gl_Position = ubo.proj * ubo.view * worldPos; // Transform to clip space
}

