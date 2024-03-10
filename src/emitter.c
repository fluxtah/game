#include "include/emitter.h"
#include "include/pipelines/pfx/pfx_pipeline_update_descriptor_sets.h"
#include "include/pipelines/pfx/pfx_compute_pipeline_config.h"
#include "include/pipelines/pfx/pfx_pipeline_config.h"
#include "include/unit_quad_vertices.h"
#include "include/pipelines/ubo_transform.h"

void setupParticleQuad(ApplicationContext *context, const CreateEmitterInfo *info, Emitter *emitter) {
    // We use the unit quad for our particles instead of a model
    ModelData *data = malloc(sizeof(ModelData));
    Vertex *unitQuadVerticesCopy = malloc(sizeof(Vertex) * 6);
    memcpy(unitQuadVerticesCopy, unitQuadVertices, sizeof(Vertex) * 6);
    data->vertices = unitQuadVerticesCopy;
    data->num_vertices = 6;
    data->num_indices = 0; // No indices

    //
    // Images (textures)
    //
    data->diffuseImageData = malloc(sizeof(ImageData));
    if (info->textureFileName != NULL) {
        readImageDataByPath(info->textureFileName, data->diffuseImageData);
    } else {
        createDefaultImageData(data->diffuseImageData, onePixelDiffuseImage);
    }
    data->normalMapImageData = malloc(sizeof(ImageData));
    createDefaultImageData(data->normalMapImageData, onePixelNormalMap);
    data->metallicRoughnessMapImageData = malloc(sizeof(ImageData));
    createDefaultImageData(data->metallicRoughnessMapImageData, onePixelMetallicRoughnessMap);

    emitter->renderResources = createRenderResourcesFromData(
            context,
            "::QUAD::",
            data
    );
}

Emitter *createEmitter(ApplicationContext *context, CreateEmitterInfo *info) {
    Emitter *emitter = malloc(sizeof(Emitter));

    emitter->renderResources = NULL;

    emitter->maxParticles = info->maxParticles;
    if (info->particleBatchSize == 0) {
        emitter->particleBatchSize = 16;
    } else {
        emitter->particleBatchSize = info->particleBatchSize;
    }

    emitter->position[0] = info->emitterPositionX;
    emitter->position[1] = info->emitterPositionY;
    emitter->position[2] = info->emitterPositionZ;
    emitter->rotation[0] = info->emitterRotationX;
    emitter->rotation[1] = info->emitterRotationY;
    emitter->rotation[2] = info->emitterRotationZ;
    emitter->scale[0] = info->emitterScaleX;
    emitter->scale[1] = info->emitterScaleY;
    emitter->scale[2] = info->emitterScaleZ;

    emitter->spawnRate = info->particleSpawnRate;
    emitter->lifeTime = info->particleLifetime;

    emitter->gravity[0] = info->particleGravityX;
    emitter->gravity[1] = info->particleGravityY;
    emitter->gravity[2] = info->particleGravityZ;
    emitter->gravity[3] = 0.0f;

    emitter->spawnPositionFrom[0] = info->particleSpawnPositionFromX;
    emitter->spawnPositionFrom[1] = info->particleSpawnPositionFromY;
    emitter->spawnPositionFrom[2] = info->particleSpawnPositionFromZ;
    emitter->spawnPositionFrom[3] = 0.0f;

    emitter->spawnPositionTo[0] = info->particleSpawnPositionToX;
    emitter->spawnPositionTo[1] = info->particleSpawnPositionToY;
    emitter->spawnPositionTo[2] = info->particleSpawnPositionToZ;
    emitter->spawnPositionTo[3] = 0.0f;

    emitter->spawnVelocityFrom[0] = info->particleSpawnVelocityFromX;
    emitter->spawnVelocityFrom[1] = info->particleSpawnVelocityFromY;
    emitter->spawnVelocityFrom[2] = info->particleSpawnVelocityFromZ;
    emitter->spawnVelocityFrom[3] = 0.0f;

    emitter->spawnVelocityTo[0] = info->particleSpawnVelocityToX;
    emitter->spawnVelocityTo[1] = info->particleSpawnVelocityToY;
    emitter->spawnVelocityTo[2] = info->particleSpawnVelocityToZ;
    emitter->spawnVelocityTo[3] = 0.0f;

    const char *shaderPath = "assets/shaders/particle.comp.spv"; // default
    if (info->computeShaderFileName != NULL) {
        shaderPath = info->computeShaderFileName;
    }

    const char *vertexShaderPath = "assets/shaders/particle.vert.spv"; // default
    if (info->vertexShaderFileName != NULL) {
        vertexShaderPath = info->vertexShaderFileName;
    }

    const char *fragmentShaderPath = "assets/shaders/particle.frag.spv"; // default
    if (info->fragmentShaderFileName != NULL) {
        fragmentShaderPath = info->fragmentShaderFileName;
    }

    emitter->computePipelineConfig = createPfxComputePipelineConfig(
            context->vulkanDeviceContext,
            context->commandPool,
            shaderPath,
            emitter->maxParticles
    );

    emitter->graphicsPipelineConfig = createPfxPipelineConfig(
            context->vulkanDeviceContext,
            context->vulkanSwapchainContext,
            context->renderPass,
            vertexShaderPath,
            fragmentShaderPath
    );

    // Dynamically allocate a BufferMemory
    emitter->transformUBO = (BufferMemory *) malloc(sizeof(BufferMemory));
    createBufferMemory(
            context->vulkanDeviceContext,
            emitter->transformUBO,
            sizeof(TransformUBO),
            VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

    if (info->modelFileName != NULL) {
        emitter->renderResources = createRenderResourcesFromFile(context, info->modelFileName);
    } else {
        setupParticleQuad(context, info, emitter);
    }

    // Create descriptor sets
    if (allocateDescriptorSet(
            context->vulkanDeviceContext->device,
            emitter->graphicsPipelineConfig->descriptorPool,
            emitter->graphicsPipelineConfig->vertexShaderDescriptorSetLayout,
            &emitter->vertexDescriptorSet) != VK_SUCCESS) {
        LOG_ERROR("Failed to allocate vertex descriptor set");
        return NULL;
    }

    if (allocateDescriptorSet(
            context->vulkanDeviceContext->device,
            emitter->graphicsPipelineConfig->descriptorPool,
            emitter->graphicsPipelineConfig->fragmentShaderDescriptorSetLayout,
            &emitter->fragmentDescriptorSet) != VK_SUCCESS) {
        LOG_ERROR("Failed to allocate fragment descriptor set");
        return NULL;
    }

    updatePfxPipelineDescriptorSets(
            context->vulkanDeviceContext->device,
            emitter->vertexDescriptorSet,
            emitter->fragmentDescriptorSet,
            emitter->transformUBO->buffer,
            emitter->renderResources->skins[0].colorMap->imageView,
            context->sampler
    );

    return emitter;
}

void setEmitterPosition(Emitter *emitter, float x, float y, float z) {
    emitter->position[0] = x;
    emitter->position[1] = y;
    emitter->position[2] = z;
}

void setEmitterRotation(Emitter *emitter, float x, float y, float z) {
    emitter->rotation[0] = x;
    emitter->rotation[1] = y;
    emitter->rotation[2] = z;
}

void setEmitterScale(Emitter *emitter, float x, float y, float z) {
    emitter->scale[0] = x;
    emitter->scale[1] = y;
    emitter->scale[2] = z;
}

void setEmitterSpawnRate(Emitter *emitter, float spawnRate) {
    emitter->spawnRate = spawnRate;
}

void setEmitterLifeTime(Emitter *emitter, float lifeTime) {
    emitter->lifeTime = lifeTime;
}

void applyEmitterChanges(Emitter *emitter) {
    glm_mat4_identity(emitter->modelMatrix);

    // Apply rotation and translation first
    glm_translate(emitter->modelMatrix, emitter->position);
    glm_rotate(emitter->modelMatrix, glm_rad(emitter->rotation[0]), (vec3) {1.0f, 0.0f, 0.0f}); // X rotation
    glm_rotate(emitter->modelMatrix, glm_rad(emitter->rotation[1]), (vec3) {0.0f, 1.0f, 0.0f}); // Y rotation
    glm_rotate(emitter->modelMatrix, glm_rad(emitter->rotation[2]), (vec3) {0.0f, 0.0f, 1.0f}); // Z rotation

    // Then apply non-uniform scaling
    glm_scale(emitter->modelMatrix, emitter->scale);
}

void resetEmitter(Emitter *emitter) {
    emitter->reset = 1;
}

void destroyEmitter(ApplicationContext *context, Emitter *emitter) {
    destroyComputePipelineConfig(
            context->vulkanDeviceContext,
            context->commandPool,
            emitter->computePipelineConfig);

    destroyPipelineConfig(
            context->vulkanDeviceContext,
            emitter->graphicsPipelineConfig);

    // Destroy UBOs
    destroyBufferMemory(context->vulkanDeviceContext, emitter->transformUBO);

    // Destroy the quad vertex buffer if it was used, otherwise destroy the render resources
    if (emitter->renderResources != NULL) {
        RenderResourcesMap *resources = getRenderResources(renderResourcesMap, emitter->renderResources->filename);

        if (resources->refs == 1) {
            deleteRenderResources(&renderResourcesMap, resources);
            destroyRenderResources(context, emitter->renderResources);
        } else {
            resources->refs--;
        }

        emitter->renderResources = NULL;
    }
}