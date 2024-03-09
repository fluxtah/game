#ifndef APP_UBO_UPDATE_H
#define APP_UBO_UPDATE_H


#include "include/pipelines/ubo_lighting.h"
#include "include/entity.h"
#include "include/camera.h"
#include "libkotlin_sdk_api.h"
#include "include/emitter.h"

#include <vulkan/vulkan.h>

void updateLightingUBO(VkDevice device, Entity *entity, Camera *camera);
void updateTransformUBO(VkDevice device, Entity *entity, Camera *camera);
void updateEmitterTransformUBO(VkDevice device, Emitter *emitter, Camera *camera);
void updateEmitterParamsUBO(VkDevice device, Emitter *emitter);
#endif // APP_UBO_UPDATE_H
