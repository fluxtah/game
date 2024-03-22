#include "include/vulkan/commandbuffer.h"
#include "include/pipelines/pfx/particle.h"
#include "include/emitter.h"
#include "include/push_constants.h"

VkCommandBuffer *allocateCommandBuffers(VkDevice device, VkCommandPool commandPool, uint32_t commandBufferCount) {
    VkCommandBuffer *commandBuffers = malloc(sizeof(VkCommandBuffer) * commandBufferCount);
    if (commandBuffers == VK_NULL_HANDLE) {
        fprintf(stderr, "Failed to allocate memory for command buffers\n");
        return VK_NULL_HANDLE;
    }

    VkCommandBufferAllocateInfo allocInfo = {};
    allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
    allocInfo.commandPool = commandPool;
    allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
    allocInfo.commandBufferCount = commandBufferCount;

    if (vkAllocateCommandBuffers(device, &allocInfo, commandBuffers) != VK_SUCCESS) {
        fprintf(stderr, "Failed to allocate command buffers\n");
        free(commandBuffers);
        return VK_NULL_HANDLE;
    }

    return commandBuffers;
}

void recordEntityCommandBuffer(
        VkCommandBuffer commandBuffer,
        VkPipeline graphicsPipeline,
        VkPipelineLayout pipelineLayout,
        EntityArray *ktEntities) {
    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

    for (size_t i = 0; i < ktEntities->size; i++) {
        Entity *entity = (Entity *) (ktEntities->entities[i]);

        VkBuffer vertexBuffers[] = {entity->renderResources->vertexBuffer->buffer};
        VkDeviceSize offsets[] = {0};
        vkCmdBindVertexBuffers(commandBuffer, 0, 1, vertexBuffers, offsets);
        vkCmdBindIndexBuffer(commandBuffer, entity->renderResources->indexBuffer->buffer, 0, VK_INDEX_TYPE_UINT16);

        VkDescriptorSet descriptorSets[] = {entity->vertexDescriptorSet, entity->fragmentDescriptorSet,
                                            entity->renderResources->skins[entity->skinIndex].textureDescriptorSet};
        vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, 3, descriptorSets, 0,
                                NULL);

        vkCmdDrawIndexed(commandBuffer, entity->renderResources->modelData->num_indices, 1, 0, 0, 0);
    }
}

void beginCommandBufferRecording(
        VkCommandBuffer commandBuffer,
        VkRenderPass renderPass,
        VkFramebuffer framebuffer,
        VkExtent2D *swapChainExtent) {
    VkCommandBufferBeginInfo beginInfo = {};
    beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
    // beginInfo.flags = VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT;               // Optional
    beginInfo.pInheritanceInfo = NULL; // Optional for primary command buffers

    if (vkBeginCommandBuffer(commandBuffer, &beginInfo) != VK_SUCCESS) {
        fprintf(stderr, "Failed to begin recording command buffer\n");
        exit(-1);
    }

    VkRenderPassBeginInfo renderPassInfo = {};
    renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
    renderPassInfo.renderPass = renderPass;
    renderPassInfo.framebuffer = framebuffer;
    renderPassInfo.renderArea.offset = (VkOffset2D) {0, 0};
    renderPassInfo.renderArea.extent = (*swapChainExtent);

    // Clear depth at the start of the render pass
    VkClearValue clearValues[2];
    clearValues[0].color = (VkClearColorValue) {{0.0f, 0.0f, 0.0f, 1.0f}}; // Clear color
    clearValues[1].depthStencil = (VkClearDepthStencilValue) {1.0f, 0};    // Clear depth

    renderPassInfo.clearValueCount = 2;
    renderPassInfo.pClearValues = clearValues;
    vkCmdBeginRenderPass(commandBuffer, &renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
}

void calculateAABBTransform(Entity *entity, int aabbIndex, mat4 transform) {
    glm_mat4_identity(transform);

    // Calculate the center of the AABB
    vec3 center;
    glm_vec3_add(entity->aabbs[aabbIndex].min, entity->aabbs[aabbIndex].max, center);
    glm_vec3_scale(center, 0.5f, center);

    // Calculate the scale needed to match the size of the AABB
    vec3 scale;
    glm_vec3_sub(entity->aabbs[aabbIndex].max, entity->aabbs[aabbIndex].min, scale);

    // Apply transformations
    glm_translate(transform, center); // Move to the center of the AABB
    glm_scale(transform, scale); // Scale to match the AABB size
}

void recordDebugLinesCommandBuffer(
        VkCommandBuffer commandBuffer,
        VkPipeline graphicsPipeline,
        VkPipelineLayout pipelineLayout,
        VkBuffer lineBuffer,
        int vertexCount,
        Camera *camera) {
    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

    mat4 transform = GLM_MAT4_IDENTITY_INIT;

    PushConstants pushConstants = {0};
    memcpy(pushConstants.view, camera->view, sizeof(mat4));
    memcpy(pushConstants.proj, camera->proj, sizeof(mat4));
    memcpy(pushConstants.model, transform, sizeof(mat4));

    // Push the transform to the shader
    vkCmdPushConstants(commandBuffer, pipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, sizeof(PushConstants),
                       &pushConstants);

    VkBuffer buffers[] = {lineBuffer};
    VkDeviceSize offsets[] = {0};
    vkCmdBindVertexBuffers(commandBuffer, 0, 1, buffers, offsets);

    vkCmdDraw(commandBuffer, vertexCount, 1, 0, 0);
}

void recordEmitterBuffer(VkCommandBuffer commandBuffer, EmitterArray *emitters) {
    for (size_t i = 0; i < emitters->size; i++) {
        Emitter *emitter = (Emitter *) (emitters->emitters[i]);
        BufferMemory *particleBuffer = emitter->computePipelineConfig->particleBuffer;
        PipelineConfig *pipelineConfig = emitter->graphicsPipelineConfig;

        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineConfig->pipeline);

        VkBuffer vertexBuffers[] = {
                emitter->renderResources->vertexBuffer->buffer,
                particleBuffer->buffer
        };
        VkDeviceSize offsets[] = {0, 0};
        vkCmdBindVertexBuffers(commandBuffer, 0, 2, vertexBuffers, offsets);
        VkDescriptorSet descriptorSets[] = {emitter->vertexDescriptorSet, emitter->fragmentDescriptorSet};
        vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineConfig->pipelineLayout, 0, 2,
                                descriptorSets, 0,
                                NULL);

        if (emitter->renderResources->indexBuffer != NULL) {
            vkCmdBindIndexBuffer(commandBuffer, emitter->renderResources->indexBuffer->buffer, 0, VK_INDEX_TYPE_UINT16);
            vkCmdDrawIndexed(commandBuffer, emitter->renderResources->modelData->num_indices, emitter->maxParticles, 0,
                             0, 0);
        } else {
            vkCmdDraw(commandBuffer, emitter->renderResources->modelData->num_vertices, emitter->maxParticles, 0, 0);
        }
    }
}

void recordEngineDebugTextSpriteBatchCommandBuffer(
        VkCommandBuffer commandBuffer,
        VkPipeline graphicsPipeline,
        VkPipelineLayout pipelineLayout,
        TextBatch *textBatch,
        VkExtent2D extent) {
    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

    // Initialize the matrices
    mat4 model = GLM_MAT4_IDENTITY_INIT;
    mat4 viewMatrix = GLM_MAT4_IDENTITY_INIT;
    mat4 projMatrix;

//    TransformUBO transformUBO = {0};
//    glm_mat4_identity(transformUBO.model);
//
//    // Apply rotation and translation first
//    glm_translate(model, (vec3) {0.0f, 0.0f, 0.0f});
//    vec3 rotation = {0.0f, 0.0f, 0.0f};
//    glm_rotate(model, glm_rad(rotation[0]), (vec3) {1.0f, 0.0f, 0.0f}); // X rotation
//    glm_rotate(model, glm_rad(rotation[1]), (vec3) {0.0f, 1.0f, 0.0f}); // Y rotation
//    glm_rotate(model, glm_rad(rotation[2]), (vec3) {0.0f, 0.0f, 1.0f}); // Z rotation
//
//    vec3 scale = {1.0f, 1.0f, 1.0f};
//    // Then apply non-uniform scaling
//    glm_scale(model, scale);


    // Set up an orthographic projection matrix
    glm_ortho(0.0f, extent.width, 0.0f, extent.height, -1.0f, 1.0f, projMatrix);

    //  projMatrix[1][1] *= -1; // Adjust for Vulkan's clip space

    // Set up your push constants
    PushConstants pushConstants = {0};
    memcpy(pushConstants.view, viewMatrix, sizeof(mat4)); // Use identity matrix for view
    memcpy(pushConstants.proj, projMatrix, sizeof(mat4)); // Use orthographic matrix for proj
    memcpy(pushConstants.model, model, sizeof(mat4)); // Model is already identity matrix

    // Push the transform to the shader
    vkCmdPushConstants(commandBuffer, pipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, sizeof(PushConstants),
                       &pushConstants);

    VkBuffer buffers[] = {textBatch->vertexBufferMemory->buffer};
    VkDeviceSize offsets[] = {0};
    vkCmdBindVertexBuffers(commandBuffer, 0, 1, buffers, offsets);

    VkDescriptorSet descriptorSets[] = {textBatch->fragmentDescriptorSet};
    vkCmdBindDescriptorSets(
            commandBuffer,
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipelineLayout, 0, 1, descriptorSets, 0,
            NULL);

    // Assuming you have a predefined way to get the number of vertices for your unit cube
    vkCmdDraw(commandBuffer, textBatch->vertexBufferSize, 1, 0, 0);
}

void recordTextSpriteBatchesCommandBuffer(
        VkCommandBuffer commandBuffer,
        VkPipeline graphicsPipeline,
        VkPipelineLayout pipelineLayout,
        TextBatchArray *textBatches,
        VkExtent2D extent) {
    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);


    for (size_t i = 0; i < textBatches->size; ++i) {
        TextBatch *textBatch = (TextBatch *) (textBatches->batches[i]);
        // Initialize the matrices
        mat4 model = GLM_MAT4_IDENTITY_INIT;
        mat4 viewMatrix = GLM_MAT4_IDENTITY_INIT;
        mat4 projMatrix;

//    TransformUBO transformUBO = {0};
//    glm_mat4_identity(transformUBO.model);
//
//    // Apply rotation and translation first
//    glm_translate(model, (vec3) {0.0f, 0.0f, 0.0f});
//    vec3 rotation = {0.0f, 0.0f, 0.0f};
//    glm_rotate(model, glm_rad(rotation[0]), (vec3) {1.0f, 0.0f, 0.0f}); // X rotation
//    glm_rotate(model, glm_rad(rotation[1]), (vec3) {0.0f, 1.0f, 0.0f}); // Y rotation
//    glm_rotate(model, glm_rad(rotation[2]), (vec3) {0.0f, 0.0f, 1.0f}); // Z rotation
//
//    vec3 scale = {1.0f, 1.0f, 1.0f};
//    // Then apply non-uniform scaling
//    glm_scale(model, scale);


        // Set up an orthographic projection matrix
        glm_ortho(0.0f, extent.width, 0.0f, extent.height, -1.0f, 1.0f, projMatrix);

        //  projMatrix[1][1] *= -1; // Adjust for Vulkan's clip space

        // Set up your push constants
        PushConstants pushConstants = {0};
        memcpy(pushConstants.view, viewMatrix, sizeof(mat4)); // Use identity matrix for view
        memcpy(pushConstants.proj, projMatrix, sizeof(mat4)); // Use orthographic matrix for proj
        memcpy(pushConstants.model, model, sizeof(mat4)); // Model is already identity matrix

        // Push the transform to the shader
        vkCmdPushConstants(commandBuffer, pipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, sizeof(PushConstants),
                           &pushConstants);

        VkBuffer buffers[] = {textBatch->vertexBufferMemory->buffer};
        VkDeviceSize offsets[] = {0};
        vkCmdBindVertexBuffers(commandBuffer, 0, 1, buffers, offsets);

        VkDescriptorSet descriptorSets[] = {textBatch->fragmentDescriptorSet};
        vkCmdBindDescriptorSets(
                commandBuffer,
                VK_PIPELINE_BIND_POINT_GRAPHICS,
                pipelineLayout, 0, 1, descriptorSets, 0,
                NULL);

        // Assuming you have a predefined way to get the number of vertices for your unit cube
        vkCmdDraw(commandBuffer, textBatch->vertexBufferSize, 1, 0, 0);
    }
}

void recordSpriteBatchesCommandBuffer(
        VkCommandBuffer commandBuffer,
        VkPipeline graphicsPipeline,
        VkPipelineLayout pipelineLayout,
        SpriteBatchArray *spriteBatches,
        VkExtent2D extent) {
    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

    for (size_t i = 0; i < spriteBatches->size; ++i) {
        SpriteBatch *spriteBatch = (SpriteBatch *) (spriteBatches->batches[i]);
        // Initialize the matrices
        mat4 model = GLM_MAT4_IDENTITY_INIT;
        mat4 viewMatrix = GLM_MAT4_IDENTITY_INIT;
        mat4 projMatrix;

//    TransformUBO transformUBO = {0};
//    glm_mat4_identity(transformUBO.model);
//
//    // Apply rotation and translation first
//    glm_translate(model, (vec3) {0.0f, 0.0f, 0.0f});
//    vec3 rotation = {0.0f, 0.0f, 0.0f};
//    glm_rotate(model, glm_rad(rotation[0]), (vec3) {1.0f, 0.0f, 0.0f}); // X rotation
//    glm_rotate(model, glm_rad(rotation[1]), (vec3) {0.0f, 1.0f, 0.0f}); // Y rotation
//    glm_rotate(model, glm_rad(rotation[2]), (vec3) {0.0f, 0.0f, 1.0f}); // Z rotation
//
//    vec3 scale = {1.0f, 1.0f, 1.0f};
//    // Then apply non-uniform scaling
//    glm_scale(model, scale);


        // Set up an orthographic projection matrix
        glm_ortho(0.0f, extent.width, 0.0f, extent.height, -1.0f, 1.0f, projMatrix);

        //  projMatrix[1][1] *= -1; // Adjust for Vulkan's clip space

        // Set up your push constants
        PushConstants pushConstants = {0};
        memcpy(pushConstants.view, viewMatrix, sizeof(mat4)); // Use identity matrix for view
        memcpy(pushConstants.proj, projMatrix, sizeof(mat4)); // Use orthographic matrix for proj
        memcpy(pushConstants.model, model, sizeof(mat4)); // Model is already identity matrix

        // Push the transform to the shader
        vkCmdPushConstants(commandBuffer, pipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, sizeof(PushConstants),
                           &pushConstants);

        VkBuffer buffers[] = {spriteBatch->vertexBufferMemory->buffer};
        VkDeviceSize offsets[] = {0};
        vkCmdBindVertexBuffers(commandBuffer, 0, 1, buffers, offsets);

        VkDescriptorSet descriptorSets[] = {spriteBatch->fragmentDescriptorSet};
        vkCmdBindDescriptorSets(
                commandBuffer,
                VK_PIPELINE_BIND_POINT_GRAPHICS,
                pipelineLayout, 0, 1, descriptorSets, 0,
                NULL);

        // Assuming you have a predefined way to get the number of vertices for your unit cube
        vkCmdDraw(commandBuffer, spriteBatch->vertexBufferSize, 1, 0, 0);
    }
}

void endCommandBufferRecording(VkCommandBuffer commandBuffer) {
    vkCmdEndRenderPass(commandBuffer);

    if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
        fprintf(stderr, "Failed to record emitter command buffer\n");
        exit(-1);
    }
}
