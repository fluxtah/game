#include "include/camera.h"

Camera *createCamera(CreateCameraInfo *info) {
    Camera *camera = malloc(sizeof(Camera));
    vec3 position = {info->positionX, info->positionY, info->positionZ};
    glm_vec3_copy(position, camera->position); // Set the camera position
    camera->yaw = 0.0f; // Yaw set to -90 degrees to face down the positive Z-axis
    camera->pitch = 0.0f; // Pitch set to 0 degrees, level with the horizon
    glm_vec3_copy((vec3) {0.0f, 1.0f, 0.0f}, camera->up); // Y is up in Vulkan
    camera->fov = info->fov;
    camera->aspectRatio = info->aspect;
    camera->nearPlane = info->near;
    camera->farPlane = info->far;
    camera->lookAtTarget = false; // Look-at-target mode disabled by default

    applyCameraChanges(camera);

    return camera;
}

Camera *createDefaultCamera() {
    CreateCameraInfo info = {
            .positionX = 0.0f,
            .positionY = 0.0f,
            .positionZ = 0.0f,
            .fov = 45.0f,
            .aspect = 1.0f,
            .near = 0.1f,
            .far = 100.0f
    };
    return createCamera(&info);
}

void applyCameraChanges(Camera *camera) {

    // FPS camera behavior based on yaw and pitch
    // Constrain the pitch
    if (camera->pitch > 89.0f) camera->pitch = 89.0f;
    if (camera->pitch < -89.0f) camera->pitch = -89.0f;

    vec3 front;
    front[0] = cosf(glm_rad(camera->pitch)) * cosf(glm_rad(camera->yaw));
    front[1] = sinf(glm_rad(camera->pitch));
    front[2] = cosf(glm_rad(camera->pitch)) * sinf(glm_rad(camera->yaw));
    glm_vec3_normalize_to(front, camera->direction); // Normalize and store in camera->direction

    vec3 target;
    glm_vec3_add(camera->position, front, target); // Calculate the look-at target

    if (camera->lookAtTarget) {
        glm_lookat(camera->position, camera->target, camera->up, camera->view); // Create the view matrix
    } else {
        glm_lookat(camera->position, target, camera->up, camera->view); // Create the view matrix
    }
    // Update the projection matrix
    glm_perspective(glm_rad(camera->fov), camera->aspectRatio, camera->nearPlane, camera->farPlane, camera->proj);
    camera->proj[1][1] *= -1; // Adjust for Vulkan's clip space
}

void setLookAtTarget(Camera *camera, vec3 target) {
    glm_vec3_copy(target, camera->target);
    camera->lookAtTarget = true;
}

// Function to disable the look-at-target mode
void disableLookAtTarget(Camera *camera) {
    camera->lookAtTarget = false;
}

void moveCameraForward(Camera *camera, float amount) {
    // Move forward
    vec3 delta;
    glm_vec3_scale(camera->direction, amount, delta);
    glm_vec3_add(camera->position, delta, camera->position);
}

void moveCameraBackward(Camera *camera, float amount) {
    vec3 delta;
    glm_vec3_scale(camera->direction, amount, delta);
    glm_vec3_sub(camera->position, delta, camera->position);
}

void moveCameraLeft(Camera *camera, float amount) {
    vec3 left, right;
    glm_vec3_crossn(camera->direction, camera->up, right);
    glm_vec3_negate_to(right, left); // Negate the right vector to get left
    glm_vec3_scale(left, amount, left);
    glm_vec3_add(camera->position, left, camera->position);
}

void moveCameraRight(Camera *camera, float amount) {
    vec3 right;
    glm_vec3_crossn(camera->direction, camera->up, right);
    glm_vec3_scale(right, amount, right);
    glm_vec3_add(camera->position, right, camera->position);
}

void pitchCamera(Camera *camera, float amount) {
    camera->pitch += amount;
}

void yawCamera(Camera *camera, float amount) {
    camera->yaw -= amount;
}

void positionCamera(Camera *camera, float x, float y, float z) {
    camera->position[0] = x;
    camera->position[1] = y;
    camera->position[2] = z;
}

void setCameraLookAt(Camera *camera, float x, float y, float z) {
    vec3 target = (vec3) {x, y, z};
    glm_vec3_copy(target, camera->target); // Update the target

    // Calculate new direction
    glm_vec3_sub(target, camera->position, camera->direction);
    glm_vec3_normalize(camera->direction);

    // Calculate yaw
    camera->yaw = atan2f(camera->direction[2], camera->direction[0]);

    // Calculate pitch
    float flatDistance = sqrtf(
            camera->direction[0] * camera->direction[0] + camera->direction[2] * camera->direction[2]);
    camera->pitch = atan2f(camera->direction[1], flatDistance);

    // Convert radians to degrees
    camera->pitch *= 180.0f / M_PI;
    camera->yaw *= 180.0f / M_PI;
}

void worldToScreenPoint(ApplicationContext *context,
                        float worldPosX, float worldPosY, float worldPosZ,
                        float *screenX, float *screenY) {
    Camera *camera = context->activeCamera;
    uint32_t screenWidth = context->vulkanSwapchainContext->swapChainExtent.width;
    uint32_t screenHeight = context->vulkanSwapchainContext->swapChainExtent.height;
    vec4 worldPosVec4 = {worldPosX, worldPosY, worldPosZ, 1.0f};
    vec4 clipSpacePos;

    // First, transform the world position to view space
    glm_mat4_mulv(camera->view, worldPosVec4, clipSpacePos); // View transformation

    // Then, transform the view space position to clip space
    glm_mat4_mulv(camera->proj, clipSpacePos, clipSpacePos); // Projection transformation

    // Perspective division to get NDC
    if (clipSpacePos[3] != 0.0f) { // Avoid division by zero
        for (int i = 0; i < 3; ++i) {
            clipSpacePos[i] /= clipSpacePos[3];
        }
    }

    // Adjusting for Vulkan's coordinate system, where Y is down
    // Convert NDC (-1 to 1 range) to screen coordinates
    *screenX = (clipSpacePos[0] + 1) * 0.5f * screenWidth;
    *screenY = (clipSpacePos[1] + 1) * 0.5f * screenHeight;

    // Clamping to ensure coordinates are within screen bounds
    *screenX = fmaxf(0, fminf(*screenX, (float)screenWidth));
    *screenY = fmaxf(0, fminf(*screenY, (float)screenHeight));

}




void destroyCamera(Camera *camera) {
    free(camera);
}
