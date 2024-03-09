#include "include/pipelines/sprite/sprite_pipeline_config.h"
#include "include/pipelines/sprite/sprite_pipeline_descriptor_pool.h"
#include "include/pipelines/sprite/sprite_pipeline_frag_descriptor_set_layout.h"
#include "include/vulkan/shaders.h"
#include "include/pipelines/sprite/sprite_pipeline_layout.h"
#include "include/pipelines/sprite/sprite_pipeline.h"
#include "include/pipelines/sprite/sprite_pipeline_renderpass.h"
#include "include/vulkan/framebuffer.h"
#include "include/vulkan/commandbuffer.h"

PipelineConfig *createSpriteShaderPipelineConfig(
        VulkanDeviceContext *context,
        VkCommandPool commandPool,
        VulkanSwapchainContext *vulkanSwapchainContext) {
    PipelineConfig *pipelineConfig = malloc(sizeof(PipelineConfig));

    //
    // Specialize the render pass for this pipeline we want to draw sprites on top of everything else
    // and not share the same depth buffer
    //
    pipelineConfig->renderPassConfig = malloc(sizeof(RenderPassConfig));
    RenderPassConfig *renderPassConfig = pipelineConfig->renderPassConfig;

    renderPassConfig->renderPass = createSpriteRenderPass(context);
    if (renderPassConfig->renderPass == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to create render pass for SPRITE shader pipeline");
        destroyPipelineConfig(context, pipelineConfig);
        return NULL;
    }

    renderPassConfig->swapChainFramebuffers = createSwapChainFramebuffers(
            context->device,
            vulkanSwapchainContext,
            renderPassConfig->renderPass);

    renderPassConfig->swapChainFramebuffersCount = vulkanSwapchainContext->swapChainImageCount;

    if (renderPassConfig->swapChainFramebuffers == NULL) {
        LOG_ERROR("Failed to create swap chain framebuffers for SPRITE shader pipeline");
        destroyPipelineConfig(context, pipelineConfig);
        return NULL;
    }

    renderPassConfig->commandBuffers = allocateCommandBuffers(
            context->device,
            commandPool,
            vulkanSwapchainContext->swapChainImageCount
    );

    if (renderPassConfig->commandBuffers == NULL) {
        LOG_ERROR("Failed to allocate command buffers for SPRITE shader pipeline");
        destroyPipelineConfig(context, pipelineConfig);
        return NULL;
    }

    //
    // Create a descriptor pool
    //
    pipelineConfig->descriptorPool = createSpritePipelineDescriptorPool(context->device);
    if (pipelineConfig->descriptorPool == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to create descriptor pool for SPRITE shader pipeline");
        destroyPipelineConfig(context, pipelineConfig);
        return NULL;
    }

    //
    // Create descriptor set layouts
    //
    pipelineConfig->vertexShaderDescriptorSetLayout = NULL; // No vertex shader descriptor set layout yet

    pipelineConfig->fragmentShaderDescriptorSetLayout = createSpritePipelineFragmentShaderDescriptorSetLayout(
            context->device);
    if (pipelineConfig->fragmentShaderDescriptorSetLayout == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to create fragment shader descriptor set layout for SPRITE shader pipeline");
        destroyPipelineConfig(context, pipelineConfig);
        return NULL;
    }

    VkShaderModule vertexShaderModule = createShaderModule(context->device, "shaders/sprite.vert.spv");
    if (vertexShaderModule == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to create vertex shader module for SPRITE shader pipeline");
        destroyPipelineConfig(context, pipelineConfig);
        return NULL;
    }

    VkShaderModule fragmentShaderModule = createShaderModule(context->device, "shaders/sprite.frag.spv");
    if (fragmentShaderModule == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to create fragment shader module for SPRITE shader pipeline");
        destroyPipelineConfig(context, pipelineConfig);
        return NULL;
    }

    pipelineConfig->pipelineLayout = createSpritePipelineLayout(
            context->device,
            pipelineConfig->fragmentShaderDescriptorSetLayout);

    if (pipelineConfig->pipelineLayout == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to create pipeline layout for SPRITE shader pipeline");
        destroyPipelineConfig(context, pipelineConfig);
        return NULL;
    }

    Viewport viewport = (Viewport) {
            0, 0,
            (float) vulkanSwapchainContext->swapChainExtent.width,
            (float) vulkanSwapchainContext->swapChainExtent.height,
            0.0f,
            1.0f
    };
    pipelineConfig->pipeline = createSpritePipeline(
            context->device,
            pipelineConfig->pipelineLayout,
            pipelineConfig->renderPassConfig->renderPass,
            viewport,
            vertexShaderModule,
            fragmentShaderModule);

    if (pipelineConfig->pipeline == VK_NULL_HANDLE) {
        LOG_ERROR("Failed to create pipeline for SPRITE shader pipeline");
        destroyPipelineConfig(context, pipelineConfig);
        return NULL;
    }

    vkDestroyShaderModule(context->device, vertexShaderModule, NULL);
    vkDestroyShaderModule(context->device, fragmentShaderModule, NULL);

    return pipelineConfig;
}
