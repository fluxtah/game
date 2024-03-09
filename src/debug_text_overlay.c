#include "include/debug_text_overlay.h"

void destroyDebugTextOverlay(ApplicationContext *context, DebugTextOverlay *overlay) {
    destroyTextBatch(context, overlay->batch);
    destroySpriteSheet(context, overlay->sheet);
    free(overlay);
}

DebugTextOverlay *createDebugTextOverlay(ApplicationContext *context) {
    CreateSpriteSheetInfo info = {
            .jsonFileName = "sheets/courier-new.json",
            .textureFileName = "sheets/courier-new.png"
    };

    DebugTextOverlay *overlay = malloc(sizeof(DebugTextOverlay));
    overlay->sheet = createSpriteSheet(context, &info);
    overlay->batch = createTextBatch(overlay->sheet);

    const char *replacementsFps[] = {"   "};
    const char *replacements[] = {"       "};
    TextElement *fpsElement = createTextElement(
            overlay->sheet,
            &(CreateTextElementInfo) {
                    .textTemplate = "%s FPS",
                    .replacements = replacementsFps,
                    .numReplacements = 1,
                    .positionX = 10.0f,
                    .positionY = 30.0f,
                    .colorR = 1.0f,
                    .colorG = 1.0f,
                    .colorB = 1.0f,
                    .colorA = 0.5f,
                    .scale = 0.3f});

    TextElement *updateTimeElement = createTextElement(
            overlay->sheet,
            &(CreateTextElementInfo) {
                    .textTemplate = "Update:%s",
                    .replacements = replacements,
                    .numReplacements = 1,
                    .positionX = 10.0f,
                    .positionY = 60.0f,
                    .colorR = 1.0f,
                    .colorG = 1.0f,
                    .colorB = 1.0f,
                    .colorA = 0.5f,
                    .scale = 0.3f});
    TextElement *collisionTimeElement = createTextElement(
            overlay->sheet,
            &(CreateTextElementInfo) {
                    .textTemplate = "Collision:%s",
                    .replacements = replacements,
                    .numReplacements = 1,
                    .positionX = 20.0f,
                    .positionY = 90.0f,
                    .colorR = 1.0f,
                    .colorG = 1.0f,
                    .colorB = 1.0f,
                    .colorA = 0.5f,
                    .scale = 0.3f});

    TextElement *renderTimeElement = createTextElement(
            overlay->sheet,
            &(CreateTextElementInfo) {
                    .textTemplate = "Render:%s",
                    .replacements = replacements,
                    .numReplacements = 1,
                    .positionX = 10.0f,
                    .positionY = 120.0f,
                    .colorR = 1.0f,
                    .colorG = 1.0f,
                    .colorB = 1.0f,
                    .colorA = 0.5f,
                    .scale = 0.3f});


    addTextElementToBatch(overlay->batch, fpsElement);
    addTextElementToBatch(overlay->batch, updateTimeElement);
    addTextElementToBatch(overlay->batch, collisionTimeElement);
    addTextElementToBatch(overlay->batch, renderTimeElement);

    initializeTextBatch(context, overlay->batch);

    int fpsElementWidth = measureTextElementWidth(fpsElement);
    int updateTimeElementWidth = measureTextElementWidth(updateTimeElement);
    int collisionTimeElementWidth = measureTextElementWidth(collisionTimeElement);
    int renderTimeElementWidth = measureTextElementWidth(renderTimeElement);

    int padRight = 16;
    int screenWidth = (int) context->vulkanSwapchainContext->swapChainExtent.width;
    updateTextElementPosition(context, overlay->batch, 0, (float) (screenWidth - fpsElementWidth - padRight), 16.0f);
    updateTextElementPosition(context, overlay->batch, 1, (float) (screenWidth - updateTimeElementWidth - padRight), 46.0f);
    updateTextElementPosition(context, overlay->batch, 2, (float) (screenWidth - collisionTimeElementWidth - padRight), 76.0f);
    updateTextElementPosition(context, overlay->batch, 3, (float) (screenWidth - renderTimeElementWidth - padRight), 106.0f);

    return overlay;
}

void updateDebugTextOverlay(
        ApplicationContext *context,
        TextBatch *batch,
        int frameCount,
        double avgUpdateTime,
        double avgCollisionTime,
        double avgRenderTime) {
    char fpsString[10];
    char avgUpdateTimeString[10];
    char avgCollisionTimeString[10];
    char avgRenderTimeString[10];
    snprintf(fpsString, sizeof(fpsString), "%d", frameCount); // Convert frameCount to string
    updateTextElementSegmentInBatch(context, batch, 0, 0, fpsString); // Update with dynamic FPS value

    snprintf(avgUpdateTimeString, sizeof(avgUpdateTimeString), "%.3fms", avgUpdateTime * 1000);
    updateTextElementSegmentInBatch(context, batch, 1, 0, avgUpdateTimeString);

    snprintf(avgCollisionTimeString, sizeof(avgCollisionTimeString), "%.3fms", avgCollisionTime * 1000);
    updateTextElementSegmentInBatch(context, batch, 2, 0, avgCollisionTimeString);

    snprintf(avgRenderTimeString, sizeof(avgRenderTimeString), "%.3fms", avgRenderTime * 1000);
    updateTextElementSegmentInBatch(context, batch, 3, 0, avgRenderTimeString);
}
