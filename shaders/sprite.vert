#version 450

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 inUV;
layout(location = 2) in vec4 inColor;

layout(location = 0) out vec3 fragPos;
layout(location = 1) out vec2 uv;
layout(location = 2) out vec4 color;

layout(push_constant) uniform PushConstants {
    mat4 model;
    mat4 view;
    mat4 proj;
} pushConstants;

void main() {
    vec4 worldPos = pushConstants.model * vec4(inPos, 1.0);
    fragPos = vec3(worldPos); // Position in world space
    uv = inUV;
    color = inColor;

    gl_Position = pushConstants.proj * pushConstants.view * worldPos; // Correct transformation to clip space
}
