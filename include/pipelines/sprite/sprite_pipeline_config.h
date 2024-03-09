#ifndef APP_PIPELINES_SPRITE_CONFIG_H
#define APP_PIPELINES_SPRITE_CONFIG_H

#include "include/pipelines/pipeline_config.h"
#include "include/application_context.h"

PipelineConfig *createSpriteShaderPipelineConfig(
        VulkanDeviceContext *context,
        VkCommandPool commandPool,
        VulkanSwapchainContext *vulkanSwapchainContext);

#endif //APP_PIPELINES_SPRITE_CONFIG_H
