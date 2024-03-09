#include "include/imagememory.h"
#include "include/vulkan/buffer.h"
#include "include/vulkan/image.h"

void imageDataToImageMemory(ApplicationContext *context, ImageData *imageData, ImageMemory *imageMemory) {
    // Create a staging buffer for the image resources
    BufferMemory *textureStagingBuffer = (BufferMemory *) malloc(sizeof(BufferMemory));
    createStagingBufferMemory(context->vulkanDeviceContext, imageData->image_size, imageData->image_data,
                              textureStagingBuffer);

    // Create the Vulkan image for the texture
    createImage(context->vulkanDeviceContext->device, context->vulkanDeviceContext->physicalDevice,
                imageData->image_width, imageData->image_height,
                VK_FORMAT_R8G8B8A8_UNORM, VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, &imageMemory->image, &imageMemory->memory);

    // Transition the image layout and copy the buffer to the image
    transitionTextureImageLayout(context->vulkanDeviceContext->device, context->commandPool,
                                 context->vulkanDeviceContext->graphicsQueue, imageMemory->image,
                                 VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
    copyBufferToImage(context->vulkanDeviceContext->device, context->commandPool,
                      context->vulkanDeviceContext->graphicsQueue, textureStagingBuffer->buffer,
                      imageMemory->image, imageData->image_width, imageData->image_height);
    transitionTextureImageLayout(context->vulkanDeviceContext->device, context->commandPool,
                                 context->vulkanDeviceContext->graphicsQueue, imageMemory->image,
                                 VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                                 VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

    // Create the image view for the texture
    imageMemory->imageView = createImageView(context->vulkanDeviceContext->device, &imageMemory->image,
                                             VK_FORMAT_R8G8B8A8_UNORM,
                                             VK_IMAGE_ASPECT_COLOR_BIT);

    // Clean up the staging buffer
    destroyBufferMemory(context->vulkanDeviceContext, textureStagingBuffer);
}

void createOffscreenImageMemory(ApplicationContext *context, uint32_t width, uint32_t height,
                                ImageMemory *offscreenImageMemory) {
    // Create the Vulkan image for off-screen rendering
    createImage(context->vulkanDeviceContext->device, context->vulkanDeviceContext->physicalDevice,
                width, height,
                VK_FORMAT_R8G8B8A8_UNORM, // Choose a suitable format for your off-screen rendering
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT |
                VK_IMAGE_USAGE_SAMPLED_BIT, // Usage for off-screen rendering and shader sampling
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, &offscreenImageMemory->image, &offscreenImageMemory->memory);

    // Initially, the image layout will be undefined, so transition it to a layout suitable for a color attachment
    transitionTextureImageLayout(context->vulkanDeviceContext->device, context->commandPool,
                                 context->vulkanDeviceContext->graphicsQueue, offscreenImageMemory->image,
                                 VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

    // Create the image view for the off-screen image
    offscreenImageMemory->imageView = createImageView(context->vulkanDeviceContext->device,
                                                      &offscreenImageMemory->image,
                                                      VK_FORMAT_R8G8B8A8_UNORM, // Must match the image format
                                                      VK_IMAGE_ASPECT_COLOR_BIT);
}


void destroyImageMemory(VkDevice device, ImageMemory *imageMemory) {
    vkDestroyImage(device, imageMemory->image, NULL);
    vkDestroyImageView(device, imageMemory->imageView, NULL);
    vkFreeMemory(device, imageMemory->memory, NULL);
    free(imageMemory);
}