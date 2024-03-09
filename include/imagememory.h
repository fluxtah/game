#ifndef APP_IMAGEMEMORY_H
#define APP_IMAGEMEMORY_H

#include <vulkan/vulkan.h>
#include "application_context.h"
#include "imagedata.h"

typedef struct ImageMemory {
    VkImage image;
    VkDeviceMemory memory;
    VkImageView imageView;
} ImageMemory;

typedef struct ApplicationContext ApplicationContext;

void imageDataToImageMemory(ApplicationContext *context, ImageData *imageData, ImageMemory *imageMemory);

void createOffscreenImageMemory(ApplicationContext *context, uint32_t width, uint32_t height,
                                ImageMemory *offscreenImageMemory);

void destroyImageMemory(VkDevice device, ImageMemory *imageMemory);

#endif //APP_IMAGEMEMORY_H
