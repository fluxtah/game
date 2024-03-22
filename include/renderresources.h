#ifndef APP_RENDEROBJECT_H
#define APP_RENDEROBJECT_H

#include "include/modeldata.h"
#include "include/pipelines/ubo_lighting.h"
#include "include/vulkan/buffer.h"
#include "include/application_context.h"
#include "include/imagememory.h"
#include "include/vulkan/descriptor.h"
#include "include/vulkan/image.h"
#include "libs/include/uthash.h"
#include "aabb.h"
#include "imagedata.h"
#include "obb.h"
#include <cglm/cglm.h>
#include <vulkan/vulkan.h>

typedef struct TextureResources {
    ImageMemory *colorMap;
    ImageMemory *normalMap;
    ImageMemory *metallicRoughnessMap;
    VkDescriptorSet textureDescriptorSet;
} TextureResources;

typedef struct RenderResources {
    char *filename;
    ModelData *modelData;
    BufferMemory *vertexBuffer;
    BufferMemory *indexBuffer;

    // Can have up to 4 textures
    TextureResources skins[4];
    int num_skins;

    AABB *aabbs;
    int num_aabbs;
} RenderResources;

typedef struct RenderResourcesMap {
    char *filename;
    int refs;
    RenderResources *resources;
    UT_hash_handle hh;
} RenderResourcesMap;

extern RenderResourcesMap *renderResourcesMap;

RenderResources *createRenderResourcesFromFile(ApplicationContext *context, const char *filename);
RenderResources *createRenderResourcesFromData(ApplicationContext *context, const char *key, ModelData *modelData);

void destroyRenderResources(ApplicationContext *context, RenderResources *obj);

RenderResourcesMap *getRenderResources(RenderResourcesMap *hashmap, const char *filename);
void deleteRenderResources(RenderResourcesMap **hashmap, RenderResourcesMap *entry);

#endif //APP_RENDEROBJECT_H
