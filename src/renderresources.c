#include "include/renderresources.h"
#include "include/pipelines/basic/basic_pipeline_update_descriptor_sets.h"
#include <dirent.h>
#include <sys/stat.h>

void addRenderResources(RenderResourcesMap **hashmap, const char *filename, RenderResources *resources) {
    RenderResourcesMap *entry = NULL;
    HASH_FIND_STR(*hashmap, filename, entry);
    if (entry == NULL) {
        entry = (RenderResourcesMap *) malloc(sizeof(RenderResourcesMap));
        entry->filename = strdup(filename); // Duplicate the filename
        entry->resources = resources;
        entry->refs = 1;
        HASH_ADD_KEYPTR(hh, *hashmap, entry->filename, strlen(entry->filename), entry);
    }
}

RenderResourcesMap *getRenderResources(RenderResourcesMap *hashmap, const char *filename) {
    RenderResourcesMap *entry = NULL;
    HASH_FIND_STR(hashmap, filename, entry);
    return (entry != NULL) ? entry : NULL;
}

void deleteRenderResources(RenderResourcesMap **hashmap, RenderResourcesMap *entry) {
    HASH_DEL(*hashmap, entry);
    free(entry->filename);
    free(entry);
}

AABB calculateAABB(const ModelData *modelData) {
    if (modelData == NULL || modelData->num_vertices == 0) {
        // Handle error: invalid model data
        return (AABB) {.min = {0, 0, 0}, .max = {0, 0, 0}};
    }

    // Initialize min and max with the first vertex
    float min[3] = {
            modelData->vertices[0].position[0],
            modelData->vertices[0].position[1],
            modelData->vertices[0].position[2],
    };
    float max[3] = {
            modelData->vertices[0].position[0],
            modelData->vertices[0].position[1],
            modelData->vertices[0].position[2],
    };

    for (size_t i = 1; i < modelData->num_vertices; ++i) {
        float *pos = modelData->vertices[i].position;

        // Update min and max coordinates
        min[0] = (pos[0] < min[0]) ? pos[0] : min[0];
        min[1] = (pos[1] < min[1]) ? pos[1] : min[1];
        min[2] = (pos[2] < min[2]) ? pos[2] : min[2];

        max[0] = (pos[0] > max[0]) ? pos[0] : max[0];
        max[1] = (pos[1] > max[1]) ? pos[1] : max[1];
        max[2] = (pos[2] > max[2]) ? pos[2] : max[2];
    }

    return (AABB) {
            .min ={min[0], min[1], min[2]},
            .max = {max[0], max[1], max[2]}
    };
}

/**
 * Extracts the directory path from a full file path.
 * @param fullPath The full path to the file.
 * @return The directory path. Caller is responsible for freeing the returned string.
 */
char* extractDirectoryPath(const char* fullPath) {
    if (fullPath == NULL) return NULL;

    // Duplicate the fullPath to work with
    char* dirPath = strdup(fullPath);
    if (dirPath == NULL) return NULL;

    // Find the last occurrence of '/' which separates directory path and filename
    char* lastSlash = strrchr(dirPath, '/');
    if (lastSlash != NULL) {
        *lastSlash = '\0'; // Replace the slash with a null terminator to end the string
    } else {
        // No slash found, implying the fullPath is a filename without a directory or in the current directory
        // In such cases, you might want to return ".", representing the current directory
        free(dirPath); // Free the allocated string
        return strdup("."); // Return "." to represent the current directory
    }

    return dirPath; // Return the modified string, which is now just the directory path
}

void loadSkins(ApplicationContext *context, RenderResources *resources, const char *modelDirectory) {
    char skinsDirectory[1024];
    snprintf(skinsDirectory, sizeof(skinsDirectory), "%s/skins", modelDirectory);

    DIR *dir = opendir(skinsDirectory);
    if (dir == NULL) {
        // Skins directory not found, handle as needed
        return;
    }

    struct dirent *entry;
    int skinIndex = 1; // Start from 1 assuming 0 is the default skin already loaded
    while ((entry = readdir(dir)) != NULL) {
        if (entry->d_type == DT_DIR && strcmp(entry->d_name, ".") != 0 && strcmp(entry->d_name, "..") != 0) {
            char basePath[1024], metalRoughPath[1024], normalPath[1024];
            snprintf(basePath, sizeof(basePath), "%s/%s/base.png", skinsDirectory, entry->d_name);
            snprintf(metalRoughPath, sizeof(metalRoughPath), "%s/%s/metalrough.png", skinsDirectory, entry->d_name);
            snprintf(normalPath, sizeof(normalPath), "%s/%s/normal.png", skinsDirectory, entry->d_name);

            ImageData *baseImageData = malloc(sizeof(ImageData));
            readImageDataByPath(basePath, baseImageData);

            resources->skins[skinIndex].colorMap = malloc(sizeof(ImageMemory));
            imageDataToImageMemory(context, baseImageData, resources->skins[skinIndex].colorMap);

            free(baseImageData->image_data);
            free(baseImageData);

            // Load metalrough.png if exists
            struct stat metalRoughStat;
            if (stat(metalRoughPath, &metalRoughStat) == 0) {
                ImageData *metalRoughImageData = malloc(sizeof(ImageData));
                readImageDataByPath(metalRoughPath, metalRoughImageData);

                resources->skins[skinIndex].metallicRoughnessMap = malloc(sizeof(ImageMemory));
                imageDataToImageMemory(context, metalRoughImageData, resources->skins[skinIndex].metallicRoughnessMap);

                free(metalRoughImageData->image_data);
                free(metalRoughImageData);
            } else {
                resources->skins[skinIndex].metallicRoughnessMap = resources->skins[0].metallicRoughnessMap;
            }

            // Load normal.png if exists
            struct stat normalStat;
            if (stat(normalPath, &normalStat) == 0) {
                ImageData *normalImageData = malloc(sizeof(ImageData));
                readImageDataByPath(normalPath, normalImageData);

                resources->skins[skinIndex].normalMap = malloc(sizeof(ImageMemory));
                imageDataToImageMemory(context, normalImageData, resources->skins[skinIndex].normalMap);

                free(normalImageData->image_data);
                free(normalImageData);
            } else {
                resources->skins[skinIndex].normalMap = resources->skins[0].normalMap;
            }

            if (allocateDescriptorSet(
                    context->vulkanDeviceContext->device,
                    context->pipelineConfig->descriptorPool,
                    context->pipelineConfig->samplerDescriptorSetLayout,
                    &resources->skins[skinIndex].textureDescriptorSet) != VK_SUCCESS) {
                LOG_ERROR("Failed to allocate fragment descriptor set");
                exit(-1);
            }

            updateBasicPipelineSamplerDescriptorSets(
                    context->vulkanDeviceContext->device,
                    resources->skins[skinIndex].textureDescriptorSet,
                    resources->skins[skinIndex].colorMap->imageView,
                    resources->skins[skinIndex].normalMap ? resources->skins[skinIndex].normalMap->imageView : VK_NULL_HANDLE,
                    resources->skins[skinIndex].metallicRoughnessMap ? resources->skins[skinIndex].metallicRoughnessMap->imageView : VK_NULL_HANDLE,
                    context->sampler
            );

            skinIndex++;
        }
    }

    closedir(dir);
}


/**
 * Loads model data from a GLB file and creates render resources. This function utilizes a hash map to manage
 * and share loaded resources. If called with a filename that has been previously
 * loaded, it returns the existing mapped RenderResources object instead of reloading from
 * the file, thus optimizing resource usage.
 *
 * Reference counting is employed to manage the lifecycle of the RenderResources. Each call
 * to this function increments the reference count for the RenderResources associated with the
 * given filename. When an Entity using this RenderResources is destroyed, it decrements the
 * reference count. Once the reference count reaches zero (i.e., no more Entities are using
 * the RenderResources), the RenderResources is deleted to free up resources.
 *
 * Note: Callers are responsible for decrementing the reference count by destroying
 * the Entity when it is no longer needed.
 *
 * @param context  The application context containing Vulkan device and command pool.
 * @param filename The path to the GLB file to load the render data from.
 * @return A pointer to the RenderResources structure containing the loaded data.
 */
RenderResources *createRenderResourcesFromFile(ApplicationContext *context, const char *filename) {
    RenderResourcesMap *existingData = getRenderResources(renderResourcesMap, filename);

    if (existingData != NULL) {
        existingData->refs++;
        return existingData->resources;
    }

    RenderResources *obj = malloc(sizeof(RenderResources));
    addRenderResources(&renderResourcesMap, filename, obj);

    obj->filename = strdup(filename);
    obj->modelData = loadModelData(filename);

    // Create vertex buffer with staging
    obj->vertexBuffer = (BufferMemory *) malloc(sizeof(BufferMemory));
    createStagedBufferMemory(context->vulkanDeviceContext, context->commandPool, obj->vertexBuffer,
                             obj->modelData->num_vertices * sizeof(Vertex),
                             VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                             obj->modelData->vertices);

    // Create index buffer with staging
    obj->indexBuffer = (BufferMemory *) malloc(sizeof(BufferMemory));
    createStagedBufferMemory(context->vulkanDeviceContext, context->commandPool, obj->indexBuffer,
                             obj->modelData->num_indices * sizeof(unsigned int),
                             VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                             obj->modelData->indices);

    obj->skins[0].colorMap = NULL;
    obj->skins[0].normalMap = NULL;
    obj->skins[0].metallicRoughnessMap = NULL;

    // Upload textures
    obj->skins[0].colorMap = malloc(sizeof(ImageMemory));
    imageDataToImageMemory(context, obj->modelData->diffuseImageData, obj->skins[0].colorMap);
    obj->skins[0].normalMap = malloc(sizeof(ImageMemory));
    imageDataToImageMemory(context, obj->modelData->normalMapImageData, obj->skins[0].normalMap);
    obj->skins[0].metallicRoughnessMap = malloc(sizeof(ImageMemory));
    imageDataToImageMemory(context, obj->modelData->metallicRoughnessMapImageData, obj->skins[0].metallicRoughnessMap);

    obj->num_skins = 1;

    if (allocateDescriptorSet(
            context->vulkanDeviceContext->device,
            context->pipelineConfig->descriptorPool,
            context->pipelineConfig->samplerDescriptorSetLayout,
            &obj->skins[0].textureDescriptorSet) != VK_SUCCESS) {
        LOG_ERROR("Failed to allocate fragment descriptor set");
        return NULL;
    }

    updateBasicPipelineSamplerDescriptorSets(
            context->vulkanDeviceContext->device,
            obj->skins[0].textureDescriptorSet,
            obj->skins[0].colorMap->imageView,
            obj->skins[0].normalMap->imageView,
            obj->skins[0].metallicRoughnessMap->imageView,
            context->sampler
    );

    // Try load skins
    char* modelDirectory = extractDirectoryPath(filename);
    if (modelDirectory != NULL) {
        loadSkins(context, obj, modelDirectory);
        free(modelDirectory);
    } else {
        printf("Failed to extract model directory from: %s\n", filename);
    }

    // Calculate AABB
    if(obj->modelData->num_aabbs > 0) {
        obj->aabbs = obj->modelData->aabbs;
        obj->num_aabbs = obj->modelData->num_aabbs;
    } else {
        // TODO this is a bit naughty but model data will clear it up later
        obj->modelData->aabbs = (AABB *) malloc(sizeof(AABB));
        obj->modelData->aabbs[0] = calculateAABB(obj->modelData);
        obj->modelData->num_aabbs = 1;

        obj->aabbs = obj->modelData->aabbs;
        obj->num_aabbs = obj->modelData->num_aabbs;
    }

    return obj;
}

RenderResources *createRenderResourcesFromData(ApplicationContext *context, const char *key, ModelData *modelData) {
    RenderResourcesMap *existingData = getRenderResources(renderResourcesMap, key);

    if (existingData != NULL) {
        existingData->refs++;
        return existingData->resources;
    }

    RenderResources *obj = malloc(sizeof(RenderResources));
    addRenderResources(&renderResourcesMap, key, obj);

    obj->filename = strdup(key);
    obj->modelData = modelData;

    // Create vertex buffer with staging
    obj->vertexBuffer = (BufferMemory *) malloc(sizeof(BufferMemory));
    createStagedBufferMemory(context->vulkanDeviceContext, context->commandPool, obj->vertexBuffer,
                             obj->modelData->num_vertices * sizeof(Vertex),
                             VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                             obj->modelData->vertices);

    // Create index buffer with staging
    if (modelData->num_indices > 0) {
        obj->indexBuffer = (BufferMemory *) malloc(sizeof(BufferMemory));
        createStagedBufferMemory(context->vulkanDeviceContext, context->commandPool, obj->indexBuffer,
                                 obj->modelData->num_indices * sizeof(unsigned int),
                                 VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                                 VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                 obj->modelData->indices);
    }

    obj->skins[0].colorMap = NULL;
    obj->skins[0].normalMap = NULL;
    obj->skins[0].metallicRoughnessMap = NULL;

    // Upload textures
    obj->skins[0].colorMap = malloc(sizeof(ImageMemory));
    imageDataToImageMemory(context, obj->modelData->diffuseImageData, obj->skins[0].colorMap);
    obj->skins[0].normalMap = malloc(sizeof(ImageMemory));
    imageDataToImageMemory(context, obj->modelData->normalMapImageData, obj->skins[0].normalMap);
    obj->skins[0].metallicRoughnessMap = malloc(sizeof(ImageMemory));
    imageDataToImageMemory(context, obj->modelData->metallicRoughnessMapImageData, obj->skins[0].metallicRoughnessMap);

    obj->num_skins = 1;

    if (allocateDescriptorSet(
            context->vulkanDeviceContext->device,
            context->pipelineConfig->descriptorPool,
            context->pipelineConfig->samplerDescriptorSetLayout,
            &obj->skins[0].textureDescriptorSet) != VK_SUCCESS) {
        LOG_ERROR("Failed to allocate fragment descriptor set");
        return NULL;
    }

    updateBasicPipelineSamplerDescriptorSets(
            context->vulkanDeviceContext->device,
            obj->skins[0].textureDescriptorSet,
            obj->skins[0].colorMap->imageView,
            obj->skins[0].normalMap->imageView,
            obj->skins[0].metallicRoughnessMap->imageView,
            context->sampler
    );

    // Calculate AABB
    if(obj->modelData->num_aabbs > 0) {
        obj->aabbs = obj->modelData->aabbs;
        obj->num_aabbs = obj->modelData->num_aabbs;
    } else {
        // TODO this is a bit naughty but model data will clear it up later
        obj->modelData->aabbs = (AABB *) malloc(sizeof(AABB));
        obj->modelData->aabbs[0] = calculateAABB(obj->modelData);
        obj->modelData->num_aabbs = 1;

        obj->aabbs = obj->modelData->aabbs;
        obj->num_aabbs = obj->modelData->num_aabbs;
    }

    return obj;
}

void destroyRenderResources(ApplicationContext *context, RenderResources *obj) {
    // Destroy resources buffers
    if(obj->indexBuffer != NULL) {
        destroyBufferMemory(context->vulkanDeviceContext, obj->indexBuffer);
    }
    if(obj->vertexBuffer != NULL) {
        destroyBufferMemory(context->vulkanDeviceContext, obj->vertexBuffer);
    }

    // Destroy maps
    destroyImageMemory(context->vulkanDeviceContext->device, obj->skins[0].colorMap);
    destroyImageMemory(context->vulkanDeviceContext->device, obj->skins[0].normalMap);
    destroyImageMemory(context->vulkanDeviceContext->device, obj->skins[0].metallicRoughnessMap);

    // Destroy model resources
    destroyModelData(obj->modelData);

    free(obj->filename);

    free(obj);
}