#ifndef APP_PIPELINES_BASIC_UPDATE_DESCRIPTOR_SETS_H
#define APP_PIPELINES_BASIC_UPDATE_DESCRIPTOR_SETS_H

#include <vulkan/vulkan.h>
#include "include/debug.h"
#include "include/pipelines/ubo_lighting.h"

void updateBasicPipelineDescriptorSets(
        VkDevice device,
        VkDescriptorSet vertexDescriptorSet,
        VkDescriptorSet fragmentDescriptorSet,
        VkBuffer transformUboBuffer,
        VkBuffer lightingUboBuffer);

void updateBasicPipelineSamplerDescriptorSets(
        VkDevice device,
        VkDescriptorSet descriptorSet,
        VkImageView baseColorImageView,
        VkImageView normalMapImageView,
        VkImageView metallicRoughnessMapImageView,
        VkSampler sampler);

#endif //APP_PIPELINES_BASIC_UPDATE_DESCRIPTOR_SETS_H
