#ifndef APP_PIPELINES_BASIC_LAYOUT_H
#define APP_PIPELINES_BASIC_LAYOUT_H
#include <vulkan/vulkan.h>
#include "include/debug.h"

VkPipelineLayout createBasicPipelineLayout(
        VkDevice device,
        VkDescriptorSetLayout vertexDescriptorSetLayout,
        VkDescriptorSetLayout fragmentDescriptorSetLayout,
        VkDescriptorSetLayout samplerDescriptorSetLayout);

#endif //APP_PIPELINES_BASIC_LAYOUT_H
