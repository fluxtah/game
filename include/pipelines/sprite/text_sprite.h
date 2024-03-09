#ifndef APP_PIPELINES_SPRITE_TEXT_SPRITE_H
#define APP_PIPELINES_SPRITE_TEXT_SPRITE_H

#include <stddef.h>
#include <cglm/vec2.h>
#include "sprite.h"

typedef struct {
    size_t startIndex; // Start index in the vertex buffer for this segment
    size_t endIndex;   // End index in the vertex buffer for this segment
} TextSegment;

typedef struct TextElement {
    size_t textLength;     // Length of the fullText
    char *fullText;
    TextSegment *segments;
    size_t numSegments;
    vec2 position;
    vec4 color;
    float scale;
    size_t textBatchStartIndex; // Index in the text batch where this element starts
    SpriteBuffer *spriteBuffer;
} TextElement;

typedef struct TextBatch {
    SpriteSheet *spriteSheet;
    size_t numElements;
    TextElement **elements;
    size_t vertexBufferSize;
    BufferMemory *vertexBufferStagingMemory;
    BufferMemory *vertexBufferMemory;
    VkDescriptorSet fragmentDescriptorSet;
} TextBatch;

void textToSprites(SpriteSheet *sheet, const char *text, SpriteBuffer *spriteBuffer);

void initializeTextElementFromTemplate(
        TextElement *template,
        const char *textTemplate, const char **replacements,
        size_t numReplacements);

TextElement *createTextElement(
        SpriteSheet *spriteSheet,
        CreateTextElementInfo *info);

int measureTextElementWidth(TextElement *element);
int measureTextElementHeight(TextElement *element);

void updateTextElementSegment(SpriteSheet *sheet, TextElement *element, size_t segmentIndex, const char *newText);

void syncTextBatchMemoryBuffers(ApplicationContext *context, TextBatch *batch);

void updateTextElementSegmentInBatch(ApplicationContext *context, TextBatch *batch, size_t elementIndex, size_t segmentIndex, const char *newText);

void destroyTextElement(TextElement *template);

TextBatch *createTextBatch(SpriteSheet *spriteSheet);

void addTextElementToBatch(TextBatch *batch, TextElement *element);

void destroyTextBatch(ApplicationContext *context, TextBatch *batch);

/**
 * Sets up graphics resources for the text batch such as the vertex buffer and descriptor sets
 * @param context
 * @param batch
 */
void initializeTextBatch(ApplicationContext *context, TextBatch *batch);

void updateTextElementPosition(ApplicationContext *context, TextBatch *batch, size_t elementIndex, float newPositionX, float newPositionY);
#endif //APP_PIPELINES_SPRITE_TEXT_SPRITE_H
