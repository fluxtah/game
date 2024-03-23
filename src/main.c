#define CGLTF_IMPLEMENTATION
#define STB_IMAGE_IMPLEMENTATION
#define DR_WAV_IMPLEMENTATION

#define PLATFORM_NONE 0
#define PLATFORM_STEAM 1

#define PLATFORM PLATFORM_NONE

#include "include/vulkan/setup.h"
#include "include/vulkan/render.h"
#include "include/pipelines/ubo_update.h"
#include "include/pipelines/pfx/pfx_compute_pipeline_config.h"
#include "include/emitter.h"
#include "include/pipelines/pfx/pfx_pipeline.h"
#include "include/pipelines/pfx/pfx_pipeline_config.h"
#include "include/pipelines/sprite/sprite.h"
#include "include/pipelines/sprite/text_sprite.h"
#include "modules/networking/include/hello.h"
#include "include/debug_text_overlay.h"
#include "modules/physics/include/physics.h"

void onPlatformLobbiesFetched(CLobby *lobbies, int count) {
    printf("Lobbies fetched: %d\n", count);
    for (int i = 0; i < count; i++) {
        printf("Lobby %d: %s\n", i, lobbies[i].name);
    }
}

int _main() {
    printf("Headless mode\n");
    bindKotlinApiHeadless();
    ktCreateApplication();

    ApplicationContext *context = createApplication();
    ktSetApplicationContext(context);

    ktInitApplication();

    double lastTime = glfwGetTime();
    double lastFrameTime = glfwGetTime();

    while (true) {
        double frameStartTime = glfwGetTime();
        double deltaTime = frameStartTime - lastFrameTime;
        lastFrameTime = frameStartTime;

        ktUpdateApplication((float) frameStartTime, (float) deltaTime);

        EntityArray *ktEntities = (EntityArray *) ktGetEntities();

        for (size_t i = 0; i < ktEntities->size; i++) {
            Entity *entity = (Entity *) (ktEntities->entities[i]);
            applyEntityChanges(entity);
        }

        ktStepPhysics();

        free(ktEntities->entities);
        free(ktEntities);
    }

    return 0;
}

int main() {

#if PLATFORM != PLATFORM_NONE
    platformInit();
    platformUserId();
    platformSetOnLobbyMatchListCallbackFunction(onPlatformLobbiesFetched);
    platformFetchLobbies();
#endif
    //
    //
    // Bind kotlin callbacks to C API functions
    //
    bindKotlinApi();

    ktCreateApplication();

    ApplicationContext *context = createApplication();
    ktSetApplicationContext(context);

    if (context == NULL) {
        printf("Something went wrong with setting up the application");
        return -1;
    }

    DebugTextOverlay *debugTextOverlay = createDebugTextOverlay(context);

    VkSemaphore imageAvailableSemaphore;
    VkSemaphore renderFinishedSemaphore;
    VkFence inFlightFence;

    // Semaphore creation for image availability
    VkSemaphoreCreateInfo semaphoreInfo = {};
    semaphoreInfo.sType = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
    if (vkCreateSemaphore(context->vulkanDeviceContext->device, &semaphoreInfo, NULL, &imageAvailableSemaphore) !=
        VK_SUCCESS ||
        vkCreateSemaphore(context->vulkanDeviceContext->device, &semaphoreInfo, NULL, &renderFinishedSemaphore) !=
        VK_SUCCESS) {
        fprintf(stderr, "Failed to create semaphores\n");
        return -1;
    }

    // Fence creation for rendering completion
    VkFenceCreateInfo fenceInfo = {};
    fenceInfo.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
    fenceInfo.flags = VK_FENCE_CREATE_SIGNALED_BIT;
    if (vkCreateFence(context->vulkanDeviceContext->device, &fenceInfo, NULL, &inFlightFence) != VK_SUCCESS) {
        fprintf(stderr, "Failed to create fence\n");
        return -1;
    }

    VkSemaphore signalSemaphores[] = {renderFinishedSemaphore};
    VkSemaphore waitSemaphores[] = {imageAvailableSemaphore};

    //
    // Init Kotlin Application
    //
    ktSetScreenSize((int) context->vulkanSwapchainContext->swapChainExtent.width,
                    (int) context->vulkanSwapchainContext->swapChainExtent.height);

    ktInitApplication();

    double lastTime = glfwGetTime();
    double lastFrameTime = glfwGetTime();
    double updateAccumulatedTime = 0.0;
    double physicsAccumulatedTime = 0.0;
    double renderAccumulatedTime = 0.0;
    int frameCount = 0;

    Camera *defaultCamera = createDefaultCamera();

    if (context->activeCamera == NULL) {
        context->activeCamera = defaultCamera;
    }

    /*
     * MAIN LOOP
     */
    while (!glfwWindowShouldClose(context->vulkanDeviceContext->window)) {
        double frameStartTime = glfwGetTime();
        double deltaTime = frameStartTime - lastFrameTime;
        lastFrameTime = frameStartTime;

#if PLATFORM != PLATFORM_NONE
        platformRunCallbacks();
#endif

        // TODO ============================================================================================================
        //  Text batch test
        frameCount++;
        if (frameStartTime - lastTime >= 1.0) { // If a second has passed
            double avgUpdateTime = updateAccumulatedTime / frameCount;
            double avgRenderTime = renderAccumulatedTime / frameCount;
            double avgCollisionTime = physicsAccumulatedTime / frameCount;

            updateDebugTextOverlay(context, debugTextOverlay->batch, frameCount, avgUpdateTime, avgCollisionTime,
                                   avgRenderTime);

            frameCount = 0;
            updateAccumulatedTime = 0.0;
            renderAccumulatedTime = 0.0;
            physicsAccumulatedTime = 0.0;
            lastTime = frameStartTime;
        }

        syncTextBatchMemoryBuffers(context, debugTextOverlay->batch);

        // ================================================================================================================

        glfwPollEvents();

        double updateStartTime = glfwGetTime();

        setListenerPosition(context->activeCamera->position[0],
                            context->activeCamera->position[1],
                            context->activeCamera->position[2]);

        ktUpdateApplication((float) frameStartTime, (float) deltaTime);

        //
        // Get the list of entities & emitters we want to render
        // TODO since we allocate memory for these arrays and free them every frame,
        //  we should have a better way of doing this
        //
        EntityArray *ktEntities = (EntityArray *) ktGetEntities();
        EmitterArray *ktEmitters = (EmitterArray *) ktGetEmitters();
        SpriteBatchArray *ktSpriteBatches = (SpriteBatchArray *) ktGetSpriteBatches();
        TextBatchArray *ktTextBatches = (TextBatchArray *) ktGetTextBatches();

        //
        // Apply changes/transformations that are needed for rendering
        //
        for (size_t i = 0; i < ktEntities->size; i++) {
            Entity *entity = (Entity *) (ktEntities->entities[i]);
            applyEntityChanges(entity);
        }

        for (size_t i = 0; i < ktEmitters->size; i++) {
            Emitter *emitter = (Emitter *) (ktEmitters->emitters[i]);
            applyEmitterChanges(emitter);
        }

        for (size_t i = 0; i < ktSpriteBatches->size; i++) {
            SpriteBatch *spriteBatch = (SpriteBatch *) (ktSpriteBatches->batches[i]);
            syncSpriteBatchMemoryBuffers(context, spriteBatch);
        }

        for (size_t i = 0; i < ktTextBatches->size; i++) {
            TextBatch *textBatch = (TextBatch *) (ktTextBatches->batches[i]);
            syncTextBatchMemoryBuffers(context, textBatch);
        }

        //
        // Run Physics & Collision Detection
        //
        double physicsStartTime = glfwGetTime();

        ktStepPhysics();

        physicsAccumulatedTime += glfwGetTime() - physicsStartTime;

        //
        // Update UBOs
        //
        for (size_t i = 0; i < ktEntities->size; i++) {
            Entity *obj = (Entity *) ktEntities->entities[i];
            updateTransformUBO(context->vulkanDeviceContext->device, obj, context->activeCamera);
            updateLightingUBO(context->vulkanDeviceContext->device, obj, context->activeCamera);
        }

        for (size_t i = 0; i < ktEmitters->size; i++) {
            Emitter *emitter = (Emitter *) ktEmitters->emitters[i];
            updateEmitterParamsUBO(context->vulkanDeviceContext->device, emitter);
            updateEmitterTransformUBO(context->vulkanDeviceContext->device, emitter, context->activeCamera);
        }

        //
        // Record command buffers
        //
        recordComputeCommandBuffer(ktEmitters, (float) deltaTime);

        for (size_t i = 0; i < context->vulkanSwapchainContext->swapChainImageCount; i++) {
            beginCommandBufferRecording(
                    context->commandBuffers[i],
                    context->renderPass,
                    context->swapChainFramebuffers[i],
                    &context->vulkanSwapchainContext->swapChainExtent);
            recordEntityCommandBuffer(
                    context->commandBuffers[i],
                    context->pipelineConfig->pipeline,
                    context->pipelineConfig->pipelineLayout,
                    ktEntities);

            recordEmitterBuffer(
                    context->commandBuffers[i],
                    ktEmitters
            );

#ifdef DEBUG
            if (context->debugBoundingVolumes) {
                void *debugLineData = getPhysicsDebugVertexData(ktGetCurrentPhysicsHandle());
                int debugVertexCount = getPhysicsDebugVertexCount(ktGetCurrentPhysicsHandle());

                updateBuffer(context,
                             context->physicsDebugBuffer,
                             debugLineData,
                             0,
                             debugVertexCount * sizeof(DebugVertex));

                //printf("Debug vertex count: %d\n", debugVertexCount);

                recordDebugLinesCommandBuffer(
                        context->commandBuffers[i],
                        context->debugPipelineConfig->pipeline,
                        context->debugPipelineConfig->pipelineLayout,
                        context->physicsDebugBuffer->buffer,
                        debugVertexCount,
                        context->activeCamera);
            }
#endif

            endCommandBufferRecording(context->commandBuffers[i]);

            RenderPassConfig *spriteRenderPassConfig = context->spritePipelineConfig->renderPassConfig;
            beginCommandBufferRecording(spriteRenderPassConfig->commandBuffers[i],
                                        spriteRenderPassConfig->renderPass,
                                        spriteRenderPassConfig->swapChainFramebuffers[i],
                                        &context->vulkanSwapchainContext->swapChainExtent);

            recordEngineDebugTextSpriteBatchCommandBuffer(
                    spriteRenderPassConfig->commandBuffers[i],
                    context->spritePipelineConfig->pipeline,
                    context->spritePipelineConfig->pipelineLayout,
                    debugTextOverlay->batch,
                    context->vulkanSwapchainContext->swapChainExtent);

            recordTextSpriteBatchesCommandBuffer(
                    spriteRenderPassConfig->commandBuffers[i],
                    context->spritePipelineConfig->pipeline,
                    context->spritePipelineConfig->pipelineLayout,
                    ktTextBatches,
                    context->vulkanSwapchainContext->swapChainExtent
            );

            recordSpriteBatchesCommandBuffer(
                    spriteRenderPassConfig->commandBuffers[i],
                    context->spritePipelineConfig->pipeline,
                    context->spritePipelineConfig->pipelineLayout,
                    ktSpriteBatches,
                    context->vulkanSwapchainContext->swapChainExtent
            );

            endCommandBufferRecording(spriteRenderPassConfig->commandBuffers[i]);
        }

        updateAccumulatedTime += glfwGetTime() - updateStartTime;

        double renderStartTime = glfwGetTime();

        uint32_t imageIndex;
        vkAcquireNextImageKHR(context->vulkanDeviceContext->device, context->vulkanSwapchainContext->swapChain,
                              UINT64_MAX, imageAvailableSemaphore, VK_NULL_HANDLE,
                              &imageIndex);

        VkCommandBuffer commandBuffersToSubmit[2 + ktEmitters->size];
        uint32_t commandBufferCount = 0;

        // Always add the primary command buffer
        commandBuffersToSubmit[commandBufferCount++] = context->commandBuffers[imageIndex];

        for (size_t i = 0; i < ktEmitters->size; i++) {
            Emitter *emitter = (Emitter *) ktEmitters->emitters[i];
            commandBuffersToSubmit[commandBufferCount++] = emitter->computePipelineConfig->commandBuffers[0];
        }

        // Sprite buffers
        commandBuffersToSubmit[commandBufferCount++] = context->spritePipelineConfig->renderPassConfig->commandBuffers[imageIndex];

        vkResetFences(context->vulkanDeviceContext->device, 1, &inFlightFence);

        renderSubmit(context->vulkanDeviceContext, waitSemaphores, signalSemaphores, inFlightFence,
                     commandBuffersToSubmit, commandBufferCount);


        renderPresent(context->vulkanDeviceContext, context->vulkanSwapchainContext->swapChain, signalSemaphores,
                      imageIndex);

        vkWaitForFences(context->vulkanDeviceContext->device, 1, &inFlightFence, VK_TRUE, UINT64_MAX);
        vkResetCommandPool(context->vulkanDeviceContext->device, context->commandPool, 0);

        free(ktEntities->entities);
        free(ktEntities);

        free(ktEmitters->emitters);
        free(ktEmitters);

        free(ktSpriteBatches->batches);
        free(ktSpriteBatches);

        free(ktTextBatches->batches);
        free(ktTextBatches);

        renderAccumulatedTime += glfwGetTime() - renderStartTime;
    }

    /*
     * CLEAN UP
     */

    destroyDebugTextOverlay(context, debugTextOverlay);

    vkWaitForFences(context->vulkanDeviceContext->device, 1, &inFlightFence, VK_TRUE, UINT64_MAX);
    vkResetFences(context->vulkanDeviceContext->device, 1, &inFlightFence);
    vkDestroySemaphore(context->vulkanDeviceContext->device, renderFinishedSemaphore, NULL);
    vkDestroySemaphore(context->vulkanDeviceContext->device, imageAvailableSemaphore, NULL);
    vkDestroyFence(context->vulkanDeviceContext->device, inFlightFence, NULL);

    ktDestroyApplication();

    context->activeCamera = NULL;
    destroyCamera(defaultCamera);

    destroyApplication(context);

    glfwTerminate();

#if PLATFORM != PLATFORM_NONE
    platformCleanup();
#endif

    return 0;
}
