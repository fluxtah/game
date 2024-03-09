#include "include/pipelines/pfx/pfx_compute_pipeline_config.h"
#include "include/pipelines/pfx/particle.h"
#include "include/emitter.h"
#include "include/pipelines/ubo_transform.h"
#include "include/pipelines/ubo_emitter.h"

ComputePipelineConfig *createPfxComputePipelineConfig(
        VulkanDeviceContext *context,
        VkCommandPool commandPool,
        const char *shaderPath,
        int maxParticles) {
    ComputePipelineConfig *config = malloc(sizeof(ComputePipelineConfig));

    config->descriptorPool = createPfxComputePipelineDescriptorPool(context->device);
    if (config->descriptorPool == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to create descriptor pool for pfx compute pipeline");
        destroyComputePipelineConfig(context, commandPool, config);
        return NULL;
    }

    config->descriptorSetLayout = createPfxComputePipelineDescriptorSetLayout(context->device);
    if (config->descriptorSetLayout == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to create descriptor set layout for pfx compute pipeline");
        destroyComputePipelineConfig(context, commandPool, config);
        return NULL;
    }

    config->pipelineLayout = createPfxComputePipelineLayout(context->device, config->descriptorSetLayout);
    if (config->pipelineLayout == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to create pipeline layout for pfx compute pipeline");
        destroyComputePipelineConfig(context, commandPool, config);
        return NULL;
    }

    config->computePipeline = createPfxComputePipeline(context->device, config->pipelineLayout, shaderPath);

    if (config->computePipeline == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to create pfx compute pipeline");
        destroyComputePipelineConfig(context, commandPool, config);
        return NULL;
    }

    config->commandBuffers = allocateCommandBuffers(context->device, commandPool, 1);
    if (config->commandBuffers == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to allocate command buffers for basic shader pipeline");
        destroyComputePipelineConfig(context, commandPool, config);
        return NULL;
    }

    VkDeviceSize bufferSize = sizeof(Particle) * maxParticles;

    config->particleBuffer = (BufferMemory *) malloc(sizeof(BufferMemory));

    createBufferMemory(context, config->particleBuffer, bufferSize,
                       VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                       VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

    config->emitterParamsUBO = (BufferMemory *) malloc(sizeof(BufferMemory));
    createBufferMemory(
            context,
            config->emitterParamsUBO,
            sizeof(EmitterParamsUBO),
            VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

    if (allocateDescriptorSet(
            context->device,
            config->descriptorPool,
            config->descriptorSetLayout,
            &config->descriptorSet) != VK_SUCCESS) {
        LOG_ERROR("Failed to allocate descriptor set for pfx compute pipeline");
        destroyComputePipelineConfig(context, commandPool, config);
    }

    VkDescriptorBufferInfo particleBufferInfo = {};
    particleBufferInfo.buffer = config->particleBuffer->buffer;
    particleBufferInfo.offset = 0;
    particleBufferInfo.range = sizeof(Particle) * maxParticles;

    VkDescriptorBufferInfo emitterParamsUBOInfo = {};
    emitterParamsUBOInfo.buffer = config->emitterParamsUBO->buffer;
    emitterParamsUBOInfo.offset = 0;
    emitterParamsUBOInfo.range = sizeof(EmitterParamsUBO);

    VkWriteDescriptorSet descriptorWrite = {};
    descriptorWrite.sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
    descriptorWrite.dstSet = config->descriptorSet;
    descriptorWrite.dstBinding = 0;
    descriptorWrite.dstArrayElement = 0;
    descriptorWrite.descriptorType = VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
    descriptorWrite.descriptorCount = 1;
    descriptorWrite.pBufferInfo = &particleBufferInfo;

    VkWriteDescriptorSet descriptorWrite1 = {};
    descriptorWrite1.sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
    descriptorWrite1.dstSet = config->descriptorSet;
    descriptorWrite1.dstBinding = 1;
    descriptorWrite1.dstArrayElement = 0;
    descriptorWrite1.descriptorType = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
    descriptorWrite1.descriptorCount = 1;
    descriptorWrite1.pBufferInfo = &emitterParamsUBOInfo;

    VkWriteDescriptorSet sets[] = {
            descriptorWrite,
            descriptorWrite1,
    };

    vkUpdateDescriptorSets(context->device, 2, sets, 0, NULL);

    return config;
}

void recordComputeCommandBuffer(EmitterArray *emitters, float deltaTime) {
    for (size_t i = 0; i < emitters->size; i++) {
        Emitter *emitter = (Emitter *) (emitters->emitters[i]);
        ComputePipelineConfig *config = emitter->computePipelineConfig;

        VkCommandBufferBeginInfo beginInfo = {};
        beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
        if (vkBeginCommandBuffer(config->commandBuffers[0], &beginInfo) != VK_SUCCESS) {
            LOG_ERROR("Failed to begin recording compute command buffer");
            return;
        }

        vkCmdBindPipeline(config->commandBuffers[0], VK_PIPELINE_BIND_POINT_COMPUTE, config->computePipeline);

        PfxComputePipelinePushConstants pushConstants = {
                .emitterPosition = {emitter->position[0], emitter->position[1], emitter->position[2], 0.0f},
                .deltaTime = deltaTime,
                .reset = emitter->reset,
        };

        // printf("Emitter position: %f, %f, %f\n", emitter->position[0], emitter->position[1], emitter->position[2]);

        emitter->reset = 0;

        vkCmdPushConstants(config->commandBuffers[0], config->pipelineLayout, VK_SHADER_STAGE_COMPUTE_BIT, 0,
                           sizeof(PfxComputePipelinePushConstants), &pushConstants);

        vkCmdBindDescriptorSets(config->commandBuffers[0], VK_PIPELINE_BIND_POINT_COMPUTE, config->pipelineLayout, 0, 1,
                                &config->descriptorSet, 0, NULL);

        uint32_t workGroupSize = emitter->particleBatchSize; // Example workgroup size
        uint32_t dispatchCount = (emitter->maxParticles + workGroupSize - 1) / workGroupSize;
        vkCmdDispatch(config->commandBuffers[0], dispatchCount, 1, 1);

        // Memory Barrier
        VkBufferMemoryBarrier bufferBarrier = {};
        bufferBarrier.sType = VK_STRUCTURE_TYPE_BUFFER_MEMORY_BARRIER;
        bufferBarrier.srcAccessMask = VK_ACCESS_SHADER_WRITE_BIT;
        bufferBarrier.dstAccessMask = VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT;
        bufferBarrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
        bufferBarrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
        bufferBarrier.buffer = config->particleBuffer->buffer;
        bufferBarrier.offset = 0;
        bufferBarrier.size = VK_WHOLE_SIZE;

        vkCmdPipelineBarrier(
                config->commandBuffers[0],
                VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT,
                VK_PIPELINE_STAGE_VERTEX_INPUT_BIT,
                0, 0, NULL, 1, &bufferBarrier, 0, NULL);

        if (vkEndCommandBuffer(config->commandBuffers[0]) != VK_SUCCESS) {
            LOG_ERROR("Failed to record compute command buffer");
        }
    }
}
