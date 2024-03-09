#ifndef APP_PIPELINES_SPRITE_SPRITE_H
#define APP_PIPELINES_SPRITE_SPRITE_H

#include "include/application_context.h"

typedef struct Sprite {
    vec2 uvmin;
    vec2 uvmax;
    int width;  // Original width of the sprite
    int height; // Original height of the sprite
} Sprite;

typedef struct SpriteVertex {
    vec3 position; // 2D position of the vertex and z for depth
    vec2 uv; // Texture coordinates for the vertex
    vec4 color; // Color of the vertex
} SpriteVertex;

#define ASCII_SIZE 256  // Size of the ASCII table

typedef struct {
    Sprite *sprites;          // Dynamic array of sprites
    Sprite *charSprites[ASCII_SIZE]; // Direct mapping of ASCII characters to sprites
    int spriteCount;          // Number of sprites
    int textureWidth;         // Width of the entire texture
    int textureHeight;        // Height of the entire texture
    // TODO we probably don't need to store the image data here after we've created the texture
    ImageData *imageData;     // Image data for the sprite sheet
    ImageMemory *imageMemory; // Image memory for the sprite sheet
} SpriteSheet;

typedef struct {
    Sprite *buffer;       // Dynamic buffer for sprite array
    size_t bufferSize;    // Current size of the buffer
    size_t bufferCapacity; // Capacity of the buffer
} SpriteBuffer;

typedef struct SpriteElement {
    vec2 position;
    vec4 color;
    float scale;
    float rotation;
    float startCrop;
    float endCrop;
    size_t spriteBatchStartIndex; // Index in the sprite batch where this element starts
    size_t spriteSheetIndex;
    Sprite *sprite;
} SpriteElement;

typedef struct SpriteBatch {
    SpriteSheet *spriteSheet;
    size_t numElements;
    SpriteElement **elements;
    size_t vertexBufferSize;
    BufferMemory *vertexBufferStagingMemory;
    BufferMemory *vertexBufferMemory;
    VkDescriptorSet fragmentDescriptorSet;
} SpriteBatch;

SpriteSheet *createSpriteSheet(ApplicationContext *context, CreateSpriteSheetInfo *info);

void destroySpriteSheet(ApplicationContext *context, SpriteSheet *sheet);

void ensureSpriteBufferCapacity(SpriteBuffer *spriteBuffer, size_t requiredCapacity);

SpriteBuffer *createSpriteBuffer();

void destroySpriteBuffer(SpriteBuffer *spriteBuffer);

void convertSpriteToVertices(Sprite *sprite,
                             size_t spriteIndex,
                             vec2 basePosition,
                             vec4 color,
                             float scale,
                             float rotation,
                             float startCrop,
                             float endCrop,
                             SpriteVertex *vertices);

SpriteElement *createSpriteElement(SpriteSheet *spriteSheet, CreateSpriteElementInfo *info);

void destroySpriteElement(SpriteElement *element);

void syncSpriteBatchMemoryBuffers(ApplicationContext *context, SpriteBatch *batch);

/**
 * Updates the sprite element in the batch with the a sprite sheet index, good for animating sprites
 *
 * @param context
 * @param batch
 * @param elementIndex
 * @param spriteSheetIndex
 */
void updateSpriteElementIndexInBatch(
        ApplicationContext *context,
        SpriteBatch *batch,
        size_t elementIndex,
        size_t spriteSheetIndex);

SpriteBatch *createSpriteBatch(SpriteSheet *spriteSheet);

void addSpriteElementToBatch(SpriteBatch *batch, SpriteElement *element);

void destroySpriteBatch(ApplicationContext *context, SpriteBatch *batch);

void initializeSpriteBatch(ApplicationContext *context, SpriteBatch *batch);

void transformSpriteElementInBatch(
        ApplicationContext *context,
        SpriteBatch *batch,
        size_t elementIndex,
        float positionX,
        float positionY,
        float scale,
        float rotation,
        float startCrop,
        float endCrop);

#endif //APP_PIPELINES_SPRITE_SPRITE_H
