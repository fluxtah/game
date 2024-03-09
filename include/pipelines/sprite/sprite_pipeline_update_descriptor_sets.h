#ifndef APP_PIPELINES_SPRITE_UPDATE_DESCRIPTOR_SETS_H
#define APP_PIPELINES_SPRITE_UPDATE_DESCRIPTOR_SETS_H

#include <vulkan/vulkan.h>
#include "include/debug.h"
#include "include/pipelines/ubo_lighting.h"

void updateSpritePipelineDescriptorSets(
        VkDevice device,
        VkDescriptorSet fragmentDescriptorSet,
        VkImageView baseColorImageView,
        VkSampler sampler);

#endif //APP_PIPELINES_SPRITE_UPDATE_DESCRIPTOR_SETS_H
