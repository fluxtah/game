#include "include/pipelines/pfx/pfx_compute_pipeline_descriptor_pool.h"

VkDescriptorPool createPfxComputePipelineDescriptorPool(VkDevice device) {
    VkDescriptorPoolSize poolSizes[] = {
            {VK_DESCRIPTOR_TYPE_STORAGE_BUFFER,         1},
            {VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1}
    };

    VkDescriptorPoolCreateInfo poolCreateInfo = {};
    poolCreateInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
    poolCreateInfo.poolSizeCount = 2;
    poolCreateInfo.pPoolSizes = poolSizes;
    poolCreateInfo.maxSets = 2;

    VkDescriptorPool descriptorPool;
    if (vkCreateDescriptorPool(device, &poolCreateInfo, NULL, &descriptorPool) != VK_SUCCESS) {
        LOG_ERROR("Failed to create descriptor pool");
        return VK_NULL_HANDLE;
    }

    return descriptorPool;
}