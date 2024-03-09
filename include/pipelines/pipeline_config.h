#ifndef APP_PIPELINES_PIPELINE_CONFIG_H
#define APP_PIPELINES_PIPELINE_CONFIG_H

#include <vulkan/vulkan.h>
#include <stdlib.h>
#include "include/vulkan_device_context.h"

typedef struct RenderPassConfig {
    VkRenderPass renderPass;
    VkFramebuffer *swapChainFramebuffers;
    VkCommandBuffer *commandBuffers;
    uint32_t swapChainFramebuffersCount;
} RenderPassConfig;

typedef struct PipelineConfig {
    VkDescriptorPool descriptorPool;
    VkDescriptorSetLayout vertexShaderDescriptorSetLayout;
    VkDescriptorSetLayout fragmentShaderDescriptorSetLayout;
    VkDescriptorSetLayout samplerDescriptorSetLayout;
    VkPipelineLayout pipelineLayout;
    VkPipeline pipeline;
    RenderPassConfig *renderPassConfig;
} PipelineConfig;

void destroyPipelineConfig(
        VulkanDeviceContext *context,
        PipelineConfig *pipelineConfig);

#endif //APP_PIPELINES_PIPELINE_CONFIG_H
