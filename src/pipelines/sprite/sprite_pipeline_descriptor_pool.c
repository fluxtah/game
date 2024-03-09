#include "include/pipelines/sprite/sprite_pipeline_descriptor_pool.h"
#include "include/debug.h"

VkDescriptorPool createSpritePipelineDescriptorPool(VkDevice device) {
    VkDescriptorPoolSize poolSizes[] = {
            {VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 10} // maps
    };

    VkDescriptorPoolCreateInfo poolInfo = {};
    poolInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
    poolInfo.poolSizeCount = 1;
    poolInfo.pPoolSizes = poolSizes;
    poolInfo.maxSets = 10; // Total number of descriptor sets

    VkDescriptorPool descriptorPool;
    if (vkCreateDescriptorPool(device, &poolInfo, NULL, &descriptorPool) != VK_SUCCESS) {
        LOG_ERROR("Failed to create sprite descriptor pool");
        return VK_NULL_HANDLE;
    }
    return descriptorPool;
}
