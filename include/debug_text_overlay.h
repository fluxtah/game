#ifndef APP_DEBUG_TEXT_OVERLAY_H
#define APP_DEBUG_TEXT_OVERLAY_H

#include "application_context.h"
#include "pipelines/sprite/text_sprite.h"

typedef struct DebugTextOverlay {
    TextBatch *batch;
    SpriteSheet *sheet;
} DebugTextOverlay;

void destroyDebugTextOverlay(ApplicationContext *context, DebugTextOverlay *overlay);

DebugTextOverlay *createDebugTextOverlay(ApplicationContext *context);

void updateDebugTextOverlay(
        ApplicationContext *context,
        TextBatch *batch,
        int frameCount,
        double avgUpdateTime,
        double avgCollisionTime,
        double avgRenderTime);

#endif //APP_DEBUG_TEXT_OVERLAY_H
