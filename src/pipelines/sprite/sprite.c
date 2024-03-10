#include "include/pipelines/sprite/sprite.h"
#include <stdio.h>
#include <stdlib.h>
#include "libs/include/cJSON.h"
#include "include/vulkan/buffer.h"
#include "include/vulkan/descriptor.h"
#include "include/pipelines/sprite/sprite_pipeline_update_descriptor_sets.h"
#include <string.h>

void updateSpriteElementAndStage(ApplicationContext *context, SpriteBatch *batch, SpriteElement *element);

SpriteSheet *createSpriteSheet(ApplicationContext *context, CreateSpriteSheetInfo *info) {
    // Read the JSON file
    FILE *file = fopen(info->jsonFileName, "rb");
    printf("Reading JSON file: %s\n", info->jsonFileName);
    if (file == NULL) return NULL;

    fseek(file, 0, SEEK_END);
    long length = ftell(file);
    fseek(file, 0, SEEK_SET);

    char *data = malloc(length + 1);
    fread(data, 1, length, file);
    fclose(file);

    // Parse the JSON
    cJSON *json = cJSON_Parse(data);
    if (json == NULL) {
        free(data);
        return NULL;
    }

    // Create and populate the SpriteSheet
    SpriteSheet *sheet = malloc(sizeof(SpriteSheet));
    sheet->textureWidth = cJSON_GetObjectItem(json, "textureWidth")->valueint;
    sheet->textureHeight = cJSON_GetObjectItem(json, "textureHeight")->valueint;

    cJSON *sprites = cJSON_GetObjectItem(json, "sprites");
    int spriteCount = cJSON_GetArraySize(sprites);
    sheet->sprites = malloc(spriteCount * sizeof(Sprite));
    memset(sheet->charSprites, 0, sizeof(sheet->charSprites)); // Initialize charSprites array
    sheet->spriteCount = spriteCount;

    for (int i = 0; i < spriteCount; i++) {
        cJSON *spriteItem = cJSON_GetArrayItem(sprites, i);
        Sprite *sprite = &sheet->sprites[i];

        // Calculate UV coordinates
        int x = cJSON_GetObjectItem(spriteItem, "x")->valueint;
        int y = cJSON_GetObjectItem(spriteItem, "y")->valueint;
        int width = cJSON_GetObjectItem(spriteItem, "width")->valueint;
        int height = cJSON_GetObjectItem(spriteItem, "height")->valueint;

        sprite->uvmin[0] = (float) x / (float) sheet->textureWidth;
        sprite->uvmin[1] = (float) y / (float) sheet->textureHeight;
        sprite->uvmax[0] = (float) (x + width) / (float) sheet->textureWidth;
        sprite->uvmax[1] = (float) (y + height) / (float) sheet->textureHeight;

        sprite->width = width;
        sprite->height = height;

        // Character mapping
        const char *character = cJSON_GetObjectItem(spriteItem, "id")->valuestring;
        if (character && strlen(character) == 1) {
            int asciiIndex = (int) character[0];
            sheet->charSprites[asciiIndex] = sprite;
        }
    }

    free(data);
    cJSON_Delete(json);

    sheet->imageData = malloc(sizeof(ImageData));
    readImageDataByPath(info->textureFileName, sheet->imageData);

    sheet->imageMemory = malloc(sizeof(ImageMemory));
    imageDataToImageMemory(context, sheet->imageData, sheet->imageMemory);

    return sheet;
}

void destroySpriteSheet(ApplicationContext *context, SpriteSheet *sheet) {
    if (sheet != NULL) {
        // Free the sprites array
        if (sheet->sprites != NULL) {
            free(sheet->sprites);
            sheet->sprites = NULL;
        }

        // Free the image data
        if (sheet->imageData != NULL) {
            free(sheet->imageData->image_data);
            free(sheet->imageData);
            sheet->imageData = NULL;
        }

        if (sheet->imageMemory != NULL) {
            destroyImageMemory(context->vulkanDeviceContext->device, sheet->imageMemory);
            sheet->imageMemory = NULL;
        }

        // Free the SpriteSheet struct itself
        free(sheet);
    }
}

void ensureSpriteBufferCapacity(SpriteBuffer *spriteBuffer, size_t requiredCapacity) {
    if (spriteBuffer->bufferCapacity < requiredCapacity) {
        spriteBuffer->buffer = realloc(spriteBuffer->buffer, requiredCapacity * sizeof(Sprite));
        spriteBuffer->bufferCapacity = requiredCapacity;
    }
}

SpriteBuffer *createSpriteBuffer() {
    SpriteBuffer *spriteBuffer = malloc(sizeof(SpriteBuffer));
    if (spriteBuffer != NULL) {
        spriteBuffer->buffer = NULL;
        spriteBuffer->bufferSize = 0;
        spriteBuffer->bufferCapacity = 0;
    }
    return spriteBuffer;
}

void destroySpriteBuffer(SpriteBuffer *spriteBuffer) {
    if (spriteBuffer != NULL) {
        // Free the internal buffer
        if (spriteBuffer->buffer != NULL) {
            free(spriteBuffer->buffer);
        }

        // Free the SpriteBuffer struct itself
        free(spriteBuffer);
    }
}

SpriteElement *createSpriteElement(SpriteSheet *spriteSheet, CreateSpriteElementInfo *info) {
    SpriteElement *element = malloc(sizeof(SpriteElement));
    if (element == NULL) return NULL;

    element->sprite = NULL;
    element->spriteBatchStartIndex = 0;
    element->position[0] = info->positionX;
    element->position[1] = info->positionY;
    element->color[0] = info->colorR;
    element->color[1] = info->colorG;
    element->color[2] = info->colorB;
    element->color[3] = info->colorA;

    if (info->scale > 0.0f) {
        element->scale = info->scale;
    } else {
        element->scale = 1.0f;
    }

    element->rotation = info->rotation;

    element->spriteSheetIndex = info->spriteSheetIndex;
    element->sprite = &spriteSheet->sprites[info->spriteSheetIndex];

    element->startCrop = info->startCrop;
    element->endCrop = info->endCrop;

    return element;
}

void destroySpriteElement(SpriteElement *element) {
    if (element != NULL) {
        element->sprite = NULL;
        free(element);
    }
}

void syncSpriteBatchMemoryBuffers(ApplicationContext *context, SpriteBatch *batch) {
    if (!context || !batch || !batch->vertexBufferStagingMemory || !batch->vertexBufferMemory) return;

    // Copy all accumulated updates from the staging buffer to the GPU buffer
    copyBuffer(
            context->vulkanDeviceContext->device,
            context->commandPool,
            context->vulkanDeviceContext->graphicsQueue,
            batch->vertexBufferStagingMemory->buffer,
            batch->vertexBufferMemory->buffer,
            0, 0, batch->vertexBufferSize * sizeof(SpriteVertex));
}

void convertSpriteToVerticesWithTransform(
        Sprite *sprite, size_t spriteIndex, vec2 basePosition, vec4 color,
        float scale, float rotation, float startCrop, float endCrop, SpriteVertex *vertices) {
    // Calculating scaled width and height only once
    float scaledWidth = (float) sprite->width * scale;
    float scaledHeight = (float) sprite->height * scale;
    float xOffset = (float) spriteIndex * scaledWidth;

    // Adjusting for cropping
    float cropWidth = scaledWidth * (endCrop - startCrop);
    xOffset += scaledWidth * startCrop;

    vec2 center = {
            basePosition[0] + xOffset + cropWidth / 2,
            basePosition[1] + scaledHeight / 2
    };
    float rad = rotation * (M_PI / 180.0f);

    // Define the quad corners (two triangles) for this sprite, scaled, offset, and cropped
    vec2 quadCorners[6] = {
            // First Triangle (CW)
            {basePosition[0] + xOffset,             basePosition[1]},                    // Bottom left
            {basePosition[0] + xOffset,             basePosition[1] + scaledHeight},     // Top left
            {basePosition[0] + xOffset + cropWidth, basePosition[1]},                    // Bottom right

            // Second Triangle (CW)
            {basePosition[0] + xOffset + cropWidth, basePosition[1]},                    // Bottom right
            {basePosition[0] + xOffset,             basePosition[1] + scaledHeight},     // Top left
            {basePosition[0] + xOffset + cropWidth, basePosition[1] + scaledHeight}      // Top right
    };

    for (int i = 0; i < 6; ++i) {
        float x = quadCorners[i][0] - center[0];
        float y = quadCorners[i][1] - center[1];

        quadCorners[i][0] = x * cosf(rad) - y * sinf(rad) + center[0];
        quadCorners[i][1] = x * sinf(rad) + y * cosf(rad) + center[1];
    }

    // Calculating UV coordinates for cropping
    float uvWidth = sprite->uvmax[0] - sprite->uvmin[0];
    float uvStart = sprite->uvmin[0] + uvWidth * startCrop;
    float uvEnd = sprite->uvmin[0] + uvWidth * endCrop;

    // Assign positions and adjusted texture coordinates to each vertex
    // First Triangle
    vertices[0].position[0] = quadCorners[0][0];
    vertices[0].position[1] = quadCorners[0][1];
    vertices[0].position[2] = 0.0f;
    vertices[0].uv[0] = uvStart;
    vertices[0].uv[1] = sprite->uvmin[1];

    vertices[1].position[0] = quadCorners[1][0];
    vertices[1].position[1] = quadCorners[1][1];
    vertices[1].position[2] = 0.0f;
    vertices[1].uv[0] = uvStart;
    vertices[1].uv[1] = sprite->uvmax[1];

    vertices[2].position[0] = quadCorners[2][0];
    vertices[2].position[1] = quadCorners[2][1];
    vertices[2].position[2] = 0.0f;
    vertices[2].uv[0] = uvEnd;
    vertices[2].uv[1] = sprite->uvmin[1];

    // Second Triangle
    vertices[3].position[0] = quadCorners[3][0];
    vertices[3].position[1] = quadCorners[3][1];
    vertices[3].position[2] = 0.0f;
    vertices[3].uv[0] = uvEnd;
    vertices[3].uv[1] = sprite->uvmin[1];

    vertices[4].position[0] = quadCorners[4][0];
    vertices[4].position[1] = quadCorners[4][1];
    vertices[4].position[2] = 0.0f;
    vertices[4].uv[0] = uvStart;
    vertices[4].uv[1] = sprite->uvmax[1];

    vertices[5].position[0] = quadCorners[5][0];
    vertices[5].position[1] = quadCorners[5][1];
    vertices[5].position[2] = 0.0f;
    vertices[5].uv[0] = uvEnd;
    vertices[5].uv[1] = sprite->uvmax[1];

    // Assign color to each vertex
    for (int i = 0; i < 6; ++i) {
        vertices[i].color[0] = color[0];
        vertices[i].color[1] = color[1];
        vertices[i].color[2] = color[2];
        vertices[i].color[3] = color[3];
    }
}

void convertSpriteToVerticesOptimized(Sprite *sprite, size_t spriteIndex, vec2 basePosition, vec4 color, SpriteVertex *vertices) {

    // Calculating scaled width and height only once
    float width = (float) sprite->width;
    float height = (float) sprite->height;
    float xOffset = (float) spriteIndex * width;

    // Define the quad corners (two triangles) for this sprite, scaled and offset
    vec2 quadCorners[6] = {
            // First Triangle (CW)
            {basePosition[0] + xOffset,         basePosition[1]},                                   // Bottom left
            {basePosition[0] + xOffset,         basePosition[1] + height},                    // Top left
            {basePosition[0] + xOffset + width, basePosition[1]},                     // Bottom right

            // Second Triangle (CW)
            {basePosition[0] + xOffset + width, basePosition[1]},                     // Bottom right
            {basePosition[0] + xOffset,         basePosition[1] + height},                    // Top left
            {basePosition[0] + xOffset + width, basePosition[1] + height}       // Top right
    };

    // Assign positions and texture coordinates to each vertex
    // First Triangle
    vertices[0].position[0] = quadCorners[0][0];
    vertices[0].position[1] = quadCorners[0][1];
    vertices[0].position[2] = 0.0f;
    vertices[0].uv[0] = sprite->uvmin[0];
    vertices[0].uv[1] = sprite->uvmin[1];

    vertices[1].position[0] = quadCorners[1][0];
    vertices[1].position[1] = quadCorners[1][1];
    vertices[1].position[2] = 0.0f;
    vertices[1].uv[0] = sprite->uvmin[0];
    vertices[1].uv[1] = sprite->uvmax[1];

    vertices[2].position[0] = quadCorners[2][0];
    vertices[2].position[1] = quadCorners[2][1];
    vertices[2].position[2] = 0.0f;
    vertices[2].uv[0] = sprite->uvmax[0];
    vertices[2].uv[1] = sprite->uvmin[1];

    // Second Triangle
    vertices[3].position[0] = quadCorners[3][0];
    vertices[3].position[1] = quadCorners[3][1];
    vertices[3].position[2] = 0.0f;
    vertices[3].uv[0] = sprite->uvmax[0];
    vertices[3].uv[1] = sprite->uvmin[1];

    vertices[4].position[0] = quadCorners[4][0];
    vertices[4].position[1] = quadCorners[4][1];
    vertices[4].position[2] = 0.0f;
    vertices[4].uv[0] = sprite->uvmin[0];
    vertices[4].uv[1] = sprite->uvmax[1];

    vertices[5].position[0] = quadCorners[5][0];
    vertices[5].position[1] = quadCorners[5][1];
    vertices[5].position[2] = 0.0f;
    vertices[5].uv[0] = sprite->uvmax[0];
    vertices[5].uv[1] = sprite->uvmax[1];

    // Assign color to each vertex
    for (int i = 0; i < 6; ++i) {
        vertices[i].color[0] = color[0];
        vertices[i].color[1] = color[1];
        vertices[i].color[2] = color[2];
        vertices[i].color[3] = color[3];
    }
}

void convertSpriteToVertices(Sprite *sprite,
                             size_t spriteIndex,
                             vec2 basePosition,
                             vec4 color,
                             float scale,
                             float rotation,
                             float startCrop,
                             float endCrop,
                             SpriteVertex *vertices) {
    if (startCrop == 0.0f && endCrop == 1.0f && scale == 1.0f && rotation == 0.0f) {
        convertSpriteToVerticesOptimized(sprite, spriteIndex, basePosition, color, vertices);
    } else {
        convertSpriteToVerticesWithTransform(
                sprite, spriteIndex, basePosition, color, scale, rotation, startCrop, endCrop, vertices);
    }
}

void updateSpriteElementIndexInBatch(
        ApplicationContext *context,
        SpriteBatch *batch,
        size_t elementIndex,
        size_t spriteSheetIndex) {
    if (!batch || elementIndex >= batch->numElements) return;

    SpriteElement *element = batch->elements[elementIndex];
    element->spriteSheetIndex = spriteSheetIndex; // Update the sprite index
    element->sprite = &batch->spriteSheet->sprites[spriteSheetIndex]; // Get the new sprite

    updateSpriteElementAndStage(context, batch, element);
}

void transformSpriteElementInBatch(
        ApplicationContext *context,
        SpriteBatch *batch,
        size_t elementIndex,
        float positionX,
        float positionY,
        float scale,
        float rotation,
        float startCrop,
        float endCrop) {
    if (!batch || elementIndex >= batch->numElements) return;

    SpriteElement *element = batch->elements[elementIndex];
    element->position[0] = positionX;
    element->position[1] = positionY;
    element->scale = scale;
    element->rotation = rotation;
    element->startCrop = startCrop;
    element->endCrop = endCrop;

    updateSpriteElementAndStage(context, batch, element);
}

void updateSpriteElementAndStage(ApplicationContext *context, SpriteBatch *batch,
                                 SpriteElement *element) {// Create a temporary buffer to hold the vertex data for this sprite
    SpriteVertex *tempVertices = malloc(sizeof(SpriteVertex) * 6);
    if (!tempVertices) {
        fprintf(stderr, "Failed to allocate memory for temporary vertices\n");
        exit(1);
    }

    // Populate the temporary vertex data
    convertSpriteToVertices(
            element->sprite,
            0,
            element->position,
            element->color,
            element->scale,
            element->rotation,
            element->startCrop,
            element->endCrop,
            tempVertices); // `0` since it's a single sprite

    // Update the sprite in the staging buffer
    size_t bufferOffset = element->spriteBatchStartIndex * sizeof(SpriteVertex);
    updateStagingBufferSegment(
            context,
            batch->vertexBufferStagingMemory,
            tempVertices,
            bufferOffset,
            sizeof(SpriteVertex) * 6);

    free(tempVertices); // Free the temporary buffer
}

SpriteBatch *createSpriteBatch(SpriteSheet *spriteSheet) {
    SpriteBatch *batch = malloc(sizeof(SpriteBatch));
    if (!batch) return NULL;

    batch->elements = NULL;
    batch->numElements = 0;
    batch->vertexBufferSize = 0;
    batch->spriteSheet = spriteSheet;
    batch->vertexBufferStagingMemory = NULL;
    batch->vertexBufferMemory = NULL;

    return batch;
}

void addSpriteElementToBatch(SpriteBatch *batch, SpriteElement *element) {
    if (!batch || !element) return;

    // Resize the elements array to accommodate the new element
    SpriteElement **newElements = realloc(batch->elements, (batch->numElements + 1) * sizeof(SpriteElement *));
    if (!newElements) return;

    batch->elements = newElements;
    batch->elements[batch->numElements] = element;
    batch->numElements++;

    // The start index for the new element in the batch's vertex buffer
    element->spriteBatchStartIndex = batch->vertexBufferSize;
    batch->vertexBufferSize += 6; // 6 vertices for a quad
}

void destroySpriteBatch(ApplicationContext *context, SpriteBatch *batch) {
    if (!batch) return;

    for (size_t i = 0; i < batch->numElements; ++i) {
        destroySpriteElement(batch->elements[i]);
    }
    free(batch->elements);
    batch->elements = NULL;

    if (batch->vertexBufferMemory) {
        destroyBufferMemory(context->vulkanDeviceContext, batch->vertexBufferMemory);
        free(batch->vertexBufferMemory);
        batch->vertexBufferMemory = NULL;
    }

    if (batch->vertexBufferStagingMemory) {
        destroyBufferMemory(context->vulkanDeviceContext, batch->vertexBufferStagingMemory);
        free(batch->vertexBufferStagingMemory);
        batch->vertexBufferStagingMemory = NULL;
    }

//    if (batch->fragmentDescriptorSet) {
//        vkFreeDescriptorSets(context->vulkanDeviceContext->device, context->spritePipelineConfig->descriptorPool, 1,
//                             &batch->fragmentDescriptorSet);
//        batch->fragmentDescriptorSet = NULL;
//    }

    free(batch);
}

void initializeSpriteBatch(ApplicationContext *context, SpriteBatch *batch) {
    if (!context || !batch || !batch->spriteSheet) return;

    // Calculate total vertex count and allocate the CPU-side buffer
    batch->vertexBufferSize = batch->numElements * 6; // 6 vertices for a quad

    SpriteVertex *vertices = malloc(batch->vertexBufferSize * sizeof(SpriteVertex));
    if (!vertices) {
        fprintf(stderr, "Failed to allocate memory for CPU vertex buffer\n");
        exit(1);
    }

    // Populate the CPU-side vertex buffer
    for (size_t i = 0; i < batch->numElements; ++i) {
        SpriteElement *element = batch->elements[i];
        convertSpriteToVertices(
                element->sprite,
                0,
                element->position,
                element->color,
                element->scale,
                element->rotation,
                element->startCrop,
                element->endCrop,
                &vertices[element->spriteBatchStartIndex]);
    }

    // Initialize the BufferMemory for the GPU buffer
    if (batch->vertexBufferMemory) {
        destroyBufferMemory(context->vulkanDeviceContext, batch->vertexBufferMemory);
    } else {
        batch->vertexBufferMemory = malloc(sizeof(BufferMemory));
    }
    createBufferMemory(context->vulkanDeviceContext, batch->vertexBufferMemory,
                       batch->vertexBufferSize * sizeof(SpriteVertex),
                       VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                       VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

    // Initialize the BufferMemory for the staging buffer and transfer data
    if (batch->vertexBufferStagingMemory) {
        destroyBufferMemory(context->vulkanDeviceContext, batch->vertexBufferStagingMemory);
    } else {
        batch->vertexBufferStagingMemory = malloc(sizeof(BufferMemory));
    }
    createStagingBufferMemory(
            context->vulkanDeviceContext,
            batch->vertexBufferSize * sizeof(SpriteVertex),
            vertices,
            batch->vertexBufferStagingMemory
    );

    // Copy data from the staging buffer to the GPU buffer
    copyBuffer(context->vulkanDeviceContext->device, context->commandPool, context->vulkanDeviceContext->graphicsQueue,
               batch->vertexBufferStagingMemory->buffer, batch->vertexBufferMemory->buffer,
               0, 0, batch->vertexBufferSize * sizeof(SpriteVertex));

    free(vertices); // Free the CPU-side buffer after transfer

    if (allocateDescriptorSet(
            context->vulkanDeviceContext->device,
            context->spritePipelineConfig->descriptorPool,
            context->spritePipelineConfig->fragmentShaderDescriptorSetLayout,
            &batch->fragmentDescriptorSet) != VK_SUCCESS) {
        LOG_ERROR("Failed to allocate sprite pipeline fragment descriptor set");
        exit(1);
    }

    updateSpritePipelineDescriptorSets(
            context->vulkanDeviceContext->device,
            batch->fragmentDescriptorSet,
            batch->spriteSheet->imageMemory->imageView,
            context->sampler
    );
}
