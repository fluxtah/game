#ifndef VULKAN_FRAMEBUFFER_H
#define VULKAN_FRAMEBUFFER_H

#include <vulkan/vulkan.h>
#include <stdio.h>
#include <stdlib.h>
#include "include/application_context.h"

VkFramebuffer *
createSwapChainFramebuffers(VkDevice device, VulkanSwapchainContext *vulkanSwapchainContext, VkRenderPass renderPass);
VkFramebuffer createOffscreenFramebuffer(VkDevice device, VkImageView offscreenImageView, VkRenderPass renderPass, uint32_t width, uint32_t height);
#endif // VULKAN_FRAMEBUFFER_H