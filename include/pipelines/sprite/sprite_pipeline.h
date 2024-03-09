#ifndef APP_PIPELINES_SPRITE_H
#define APP_PIPELINES_SPRITE_H

#include "include/pipelines/viewport_util.h"

VkPipeline createSpritePipeline(
        VkDevice device, VkPipelineLayout pipelineLayout, VkRenderPass renderPass, Viewport viewport,
        VkShaderModule vertShaderModule, VkShaderModule fragShaderModule);

#endif //APP_PIPELINES_SPRITE_H
