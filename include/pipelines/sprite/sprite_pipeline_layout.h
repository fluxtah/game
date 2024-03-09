#ifndef APP_PIPELINES_SPRITE_LAYOUT_H
#define APP_PIPELINES_SPRITE_LAYOUT_H
#include <vulkan/vulkan.h>
#include "include/debug.h"

VkPipelineLayout createSpritePipelineLayout(
        VkDevice device,
        VkDescriptorSetLayout fragmentDescriptorSetLayout);

#endif //APP_PIPELINES_SPRITE_LAYOUT_H
