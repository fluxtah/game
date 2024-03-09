#include <string.h>
#include "include/pipelines/sprite/text_sprite.h"
#include "include/vulkan/buffer.h"
#include "include/vulkan/descriptor.h"
#include "include/pipelines/sprite/sprite_pipeline_update_descriptor_sets.h"

void textToSprites(SpriteSheet *sheet, const char *text, SpriteBuffer *spriteBuffer) {
    if (text == NULL || sheet == NULL || spriteBuffer == NULL) return;

    size_t length = strlen(text);
    ensureSpriteBufferCapacity(spriteBuffer, length);

    for (size_t i = 0; i < length; ++i) {
        int asciiIndex = (int) text[i];
        if (asciiIndex >= 0 && asciiIndex < ASCII_SIZE && sheet->charSprites[asciiIndex] != NULL) {
            spriteBuffer->buffer[i] = *(sheet->charSprites[asciiIndex]);
        }
        // TODO Handle undefined chars as necessary
    }

    spriteBuffer->bufferSize = length;
}

void initializeTextElementFromTemplate(TextElement *template, const char *textTemplate, const char **replacements,
                                       size_t numReplacements) {

    if (numReplacements == 0) {
        size_t textTemplateLength = strlen(textTemplate);
        template->fullText = malloc(textTemplateLength + 1); // +1 for null terminator
        strcpy(template->fullText, textTemplate); // strcpy copies the null terminator
        template->textLength = textTemplateLength;
        template->segments = NULL; // No segments needed
        template->numSegments = 0;
        return;
    }

    size_t bufferSize = strlen(textTemplate) + 1; // Exact size of the original template
    for (size_t i = 0; i < numReplacements; ++i) {
        bufferSize += strlen(replacements[i]); // Add space for replacements
    }
    char *fullTextTemp = malloc(bufferSize); // Allocate based on total size needed

    template->segments = malloc(numReplacements * sizeof(TextSegment));
    template->numSegments = numReplacements;

    size_t currentPos = 0; // Tracks the current position in fullTextTemp
    const char *nextPart = textTemplate; // Pointer to the current part of the template being processed

    for (size_t i = 0; i < numReplacements; ++i) {
        char *placeholder = strstr(nextPart, "%s");
        if (!placeholder) break;

        size_t partLength = placeholder - nextPart; // Length of the static part before the placeholder
        memcpy(fullTextTemp + currentPos, nextPart, partLength); // Copy static part
        memcpy(fullTextTemp + currentPos + partLength, replacements[i], strlen(replacements[i])); // Copy replacement

        currentPos += partLength + strlen(replacements[i]); // Update current position

        nextPart = placeholder + 2; // Move past the placeholder

        template->segments[i].startIndex = currentPos - strlen(replacements[i]);
        template->segments[i].endIndex = currentPos;
    }

    // Copy any remaining part of the template after the last replacement
    strcpy(fullTextTemp + currentPos, nextPart);
    currentPos += strlen(nextPart);

    template->fullText = malloc(currentPos + 1); // Allocate exact space needed for the full text
    memcpy(template->fullText, fullTextTemp, currentPos + 1); // Copy the built string into template

    template->textLength = currentPos; // Set the correct text length

    free(fullTextTemp); // Free the temporary buffer
}

TextElement *createTextElement(
        SpriteSheet *spriteSheet,
        CreateTextElementInfo *info) {
    TextElement *element = malloc(sizeof(TextElement));
    if (element == NULL) return NULL;

    // Initialize template members to NULL or zero
    element->fullText = NULL;
    element->segments = NULL;
    element->numSegments = 0;
    element->textBatchStartIndex = 0;
    element->position[0] = info->positionX;
    element->position[1] = info->positionY;
    element->color[0] = info->colorR;
    element->color[1] = info->colorG;
    element->color[2] = info->colorB;
    element->color[3] = info->colorA;
    element->spriteBuffer = createSpriteBuffer();

    if (info->scale > 0.0f) {
        element->scale = info->scale;
    } else {
        element->scale = 1.0f;
    }

    initializeTextElementFromTemplate(element, info->textTemplate, info->replacements, info->numReplacements);

    textToSprites(spriteSheet, element->fullText, element->spriteBuffer);

    return element;
}

int measureTextElementWidth(TextElement *element) {
    if (element == NULL) return 0;

    int width = 0;
    for (size_t i = 0; i < element->textLength; i++) {
        width += element->spriteBuffer->buffer[i].width;
    }
    width = (int) ((float) width * element->scale);

    return width;
}

int measureTextElementHeight(TextElement *element) {
    if (element == NULL) return 0;

    int height = 0;
    for (size_t i = 0; i < element->textLength; i++) {
        int spriteHeight = element->spriteBuffer->buffer[i].height;
        if (spriteHeight > height) {
            height = spriteHeight;
        }
    }
    height = (int) ((float) height * element->scale);

    return height;
}

void updateTextElementSegment(SpriteSheet *sheet, TextElement *element, size_t segmentIndex, const char *newText) {
    if (element == NULL || newText == NULL || sheet == NULL || segmentIndex >= element->numSegments) return;

    TextSegment segment = element->segments[segmentIndex];
    size_t segmentLength = segment.endIndex - segment.startIndex;
    size_t newTextLength = strlen(newText);

    // Truncate newText if it's longer than the fixed segment length
    if (newTextLength > segmentLength) {
        newTextLength = segmentLength;
    }

    // Calculate the buffer index for this segment
    size_t spriteBufferIndex = segment.startIndex; // Assuming each character maps to one sprite

    // Replace the segment text in the fullText and update the corresponding sprites
    for (size_t i = 0; i < segmentLength; ++i) {
        if (i < newTextLength) {
            // Update with new text
            element->fullText[segment.startIndex + i] = newText[i];
            int asciiIndex = (int) newText[i];
            if (asciiIndex >= 0 && asciiIndex < ASCII_SIZE && sheet->charSprites[asciiIndex] != NULL) {
                element->spriteBuffer->buffer[spriteBufferIndex + i] = *(sheet->charSprites[asciiIndex]);
            }
        } else {
            // Fill with space if newText is shorter
            element->fullText[segment.startIndex + i] = ' ';
            element->spriteBuffer->buffer[spriteBufferIndex + i] = *(sheet->charSprites[(int) ' ']);
        }
    }
}

void syncTextBatchMemoryBuffers(ApplicationContext *context, TextBatch *batch) {
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

void updateTextElementSegmentInBatch(ApplicationContext *context, TextBatch *batch, size_t elementIndex,
                                     size_t segmentIndex, const char *newText) {
    if (!batch || elementIndex >= batch->numElements) return;

    TextElement *element = batch->elements[elementIndex];
    updateTextElementSegment(batch->spriteSheet, element, segmentIndex, newText);

    // Calculate the indices for the segment in the vertex buffer
    size_t vertexStartIndex = element->textBatchStartIndex + element->segments[segmentIndex].startIndex * 6;
    size_t segmentVertexCount =
            (element->segments[segmentIndex].endIndex - element->segments[segmentIndex].startIndex) * 6;

    // Create a temporary buffer to hold the vertex data for this segment
    SpriteVertex *tempVertices = malloc(segmentVertexCount * sizeof(SpriteVertex));
    if (!tempVertices) {
        fprintf(stderr, "Failed to allocate memory for temporary vertices\n");
        exit(1);
    }

    // Populate the temporary vertex data
    for (size_t i = 0, j = element->segments[segmentIndex].startIndex; i < segmentVertexCount; i += 6, ++j) {
        Sprite *sprite = &element->spriteBuffer->buffer[j];
        convertSpriteToVertices(
                sprite,
                j,
                element->position,
                element->color,
                element->scale,
                0,
                0,
                1.0f,
                &tempVertices[i]);
    }

    // Update the segment in the staging buffer
    size_t bufferOffset = vertexStartIndex * sizeof(SpriteVertex);
    updateStagingBufferSegment(
            context,
            batch->vertexBufferStagingMemory,
            tempVertices,
            bufferOffset,
            segmentVertexCount * sizeof(SpriteVertex));

    free(tempVertices); // Free the temporary buffer
}

void destroyTextElement(TextElement *template) {
    if (template) {
        free(template->fullText);
        template->fullText = NULL;
        free(template->segments);
        template->segments = NULL;
        destroySpriteBuffer(template->spriteBuffer);
        template->spriteBuffer = NULL;
        free(template);
    }
}

TextBatch *createTextBatch(SpriteSheet *spriteSheet) {
    TextBatch *batch = malloc(sizeof(TextBatch));
    if (!batch) return NULL;

    batch->elements = NULL;
    batch->numElements = 0;
    batch->vertexBufferSize = 0;
    batch->spriteSheet = spriteSheet;
    batch->vertexBufferStagingMemory = NULL;
    batch->vertexBufferMemory = NULL;

    return batch;
}

void addTextElementToBatch(TextBatch *batch, TextElement *element) {
    if (!batch || !element) return;

    // Resize the elements array to accommodate the new element
    TextElement **newElements = realloc(batch->elements, (batch->numElements + 1) * sizeof(TextElement *));
    if (!newElements) return;

    batch->elements = newElements;
    batch->elements[batch->numElements] = element;
    batch->numElements++;

    // Calculate the vertex count for the new element (assuming 6 vertices per character)
    size_t vertexCountForElement = element->textLength * 6;

    // The start index for the new element in the batch's vertex buffer
    element->textBatchStartIndex = batch->vertexBufferSize;
    batch->vertexBufferSize += vertexCountForElement;
}

void destroyTextBatch(ApplicationContext *context, TextBatch *batch) {
    if (!batch) return;

    for (size_t i = 0; i < batch->numElements; ++i) {
        destroyTextElement(batch->elements[i]);
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

void initializeTextBatch(ApplicationContext *context, TextBatch *batch) {
    if (!context || !batch || !batch->spriteSheet) return;

    // Calculate total vertex count and allocate the CPU-side buffer
    batch->vertexBufferSize = 0;
    for (size_t i = 0; i < batch->numElements; ++i) {
        batch->vertexBufferSize += batch->elements[i]->textLength * 6; // 6 vertices per character
    }

    SpriteVertex *cpuVertexBuffer = malloc(batch->vertexBufferSize * sizeof(SpriteVertex));
    if (!cpuVertexBuffer) {
        fprintf(stderr, "Failed to allocate memory for CPU vertex buffer\n");
        exit(1);
    }

    // Populate the CPU-side vertex buffer
    size_t currentVertexIndex = 0;
    for (size_t i = 0; i < batch->numElements; ++i) {
        TextElement *element = batch->elements[i];
        for (size_t j = 0; j < element->textLength; ++j) {
            Sprite *sprite = &element->spriteBuffer->buffer[j];
            convertSpriteToVertices(
                    sprite,
                    j,
                    element->position,
                    element->color,
                    element->scale, 0,
                    0,
                    1.0f,
                    &cpuVertexBuffer[currentVertexIndex]);
            currentVertexIndex += 6; // Move to the next set of vertices
        }
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
            cpuVertexBuffer,
            batch->vertexBufferStagingMemory
    );

    // Copy data from the staging buffer to the GPU buffer
    copyBuffer(context->vulkanDeviceContext->device, context->commandPool, context->vulkanDeviceContext->graphicsQueue,
               batch->vertexBufferStagingMemory->buffer, batch->vertexBufferMemory->buffer,
               0, 0, batch->vertexBufferSize * sizeof(SpriteVertex));

    free(cpuVertexBuffer); // Free the CPU-side buffer after transfer

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

void updateTextElementPosition(ApplicationContext *context, TextBatch *batch, size_t elementIndex, float newPositionX, float newPositionY) {
    if (!batch || elementIndex >= batch->numElements) return;

    TextElement *element = batch->elements[elementIndex];
    element->position[0] = newPositionX;
    element->position[1] = newPositionY;

    size_t vertexCount = element->textLength * 6;

    SpriteVertex *tempVertices = malloc(vertexCount * sizeof(SpriteVertex));
    if (!tempVertices) {
        fprintf(stderr, "Failed to allocate memory for temporary vertices\n");
        exit(1);
    }

    // Populate the temporary vertex data
    for (size_t i = 0, j = 0; i < vertexCount; i += 6, ++j) {
        Sprite *sprite = &element->spriteBuffer->buffer[j];
        convertSpriteToVertices(
                sprite,
                j,
                element->position,
                element->color,
                element->scale,
                0,
                0,
                1.0f,
                &tempVertices[i]);
    }

    // Update the segment in the staging buffer
    size_t bufferOffset = element->textBatchStartIndex * sizeof(SpriteVertex);
    updateStagingBufferSegment(
            context,
            batch->vertexBufferStagingMemory,
            tempVertices,
            bufferOffset,
            vertexCount * sizeof(SpriteVertex));

    free(tempVertices); // Free the temporary buffer
}
