#include "include/pipelines/pipeline_config.h"

void destroyPipelineConfig(VulkanDeviceContext *context, PipelineConfig *pipelineConfig) {
    if (pipelineConfig->descriptorPool != VK_NULL_HANDLE) {
        vkDestroyDescriptorPool(context->device, pipelineConfig->descriptorPool, NULL);
        pipelineConfig->descriptorPool = VK_NULL_HANDLE;
    }

    if (pipelineConfig->vertexShaderDescriptorSetLayout != VK_NULL_HANDLE) {
        vkDestroyDescriptorSetLayout(context->device, pipelineConfig->vertexShaderDescriptorSetLayout, NULL);
        pipelineConfig->vertexShaderDescriptorSetLayout = VK_NULL_HANDLE;
    }

    if (pipelineConfig->fragmentShaderDescriptorSetLayout != VK_NULL_HANDLE) {
        vkDestroyDescriptorSetLayout(context->device, pipelineConfig->fragmentShaderDescriptorSetLayout, NULL);
        pipelineConfig->fragmentShaderDescriptorSetLayout = VK_NULL_HANDLE;
    }

    if (pipelineConfig->samplerDescriptorSetLayout != VK_NULL_HANDLE) {
        vkDestroyDescriptorSetLayout(context->device, pipelineConfig->samplerDescriptorSetLayout, NULL);
        pipelineConfig->samplerDescriptorSetLayout = VK_NULL_HANDLE;
    }

    if (pipelineConfig->pipelineLayout != VK_NULL_HANDLE) {
        vkDestroyPipelineLayout(context->device, pipelineConfig->pipelineLayout, NULL);
        pipelineConfig->pipelineLayout = VK_NULL_HANDLE;
    }

    if (pipelineConfig->pipeline != VK_NULL_HANDLE) {
        vkDestroyPipeline(context->device, pipelineConfig->pipeline, NULL);
        pipelineConfig->pipeline = VK_NULL_HANDLE;
    }

    if (pipelineConfig->renderPassConfig != NULL) {
        if (pipelineConfig->renderPassConfig->renderPass != VK_NULL_HANDLE) {
            vkDestroyRenderPass(context->device, pipelineConfig->renderPassConfig->renderPass, NULL);
            pipelineConfig->renderPassConfig->renderPass = VK_NULL_HANDLE;
        }

        if (pipelineConfig->renderPassConfig->swapChainFramebuffers != NULL) {
            for (uint32_t i = 0; i < pipelineConfig->renderPassConfig->swapChainFramebuffersCount; i++) {
                vkDestroyFramebuffer(context->device, pipelineConfig->renderPassConfig->swapChainFramebuffers[i], NULL);
            }

            free(pipelineConfig->renderPassConfig->swapChainFramebuffers);
            pipelineConfig->renderPassConfig->swapChainFramebuffers = NULL;
        }

        if (pipelineConfig->renderPassConfig->commandBuffers != NULL) {
            free(pipelineConfig->renderPassConfig->commandBuffers);
            pipelineConfig->renderPassConfig->commandBuffers = NULL;
        }

        free(pipelineConfig->renderPassConfig);
        pipelineConfig->renderPassConfig = NULL;
    }

    free(pipelineConfig);
}