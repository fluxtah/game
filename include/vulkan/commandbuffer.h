#ifndef VULKAN_COMMANDBUFFER_H
#define VULKAN_COMMANDBUFFER_H

#include "include/entity.h"
#include "include/pipelines/sprite/text_sprite.h"

void beginCommandBufferRecording(
        VkCommandBuffer commandBuffer,
        VkRenderPass renderPass,
        VkFramebuffer framebuffer,
        VkExtent2D *swapChainExtent);

VkCommandBuffer *allocateCommandBuffers(VkDevice device, VkCommandPool commandPool, uint32_t commandBufferCount);

void recordEntityCommandBuffer(
        VkCommandBuffer commandBuffer,
        VkPipeline graphicsPipeline,
        VkPipelineLayout pipelineLayout,
        EntityArray *ktEntities);

void recordDebugCommandBuffer(
        VkCommandBuffer commandBuffer,
        VkPipeline graphicsPipeline,
        VkPipelineLayout pipelineLayout,
        EntityArray *ktEntities,
        VkBuffer unitCubeVertexBuffer,
        Camera *camera);

void recordDebugLinesCommandBuffer(
        VkCommandBuffer commandBuffer,
        VkPipeline graphicsPipeline,
        VkPipelineLayout pipelineLayout,
        VkBuffer lineBuffer,
        int vertexCount,
        Camera *camera);

void recordEmitterBuffer(
        VkCommandBuffer commandBuffer,
        EmitterArray *emitters
);

void recordEngineDebugTextSpriteBatchCommandBuffer(
        VkCommandBuffer commandBuffer,
        VkPipeline graphicsPipeline,
        VkPipelineLayout pipelineLayout,
        TextBatch *textBatch,
        VkExtent2D extent);

void recordTextSpriteBatchesCommandBuffer(
        VkCommandBuffer commandBuffer,
        VkPipeline graphicsPipeline,
        VkPipelineLayout pipelineLayout,
        TextBatchArray *textBatches,
        VkExtent2D extent);

void recordSpriteBatchesCommandBuffer(
        VkCommandBuffer commandBuffer,
        VkPipeline graphicsPipeline,
        VkPipelineLayout pipelineLayout,
        SpriteBatchArray *textBatches,
        VkExtent2D extent);

void endCommandBufferRecording(VkCommandBuffer commandBuffer);

#endif // VULKAN_COMMANDBUFFER_H