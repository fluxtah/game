#include "include/vulkan/framebuffer.h"

VkFramebuffer *createSwapChainFramebuffers(VkDevice device, VulkanSwapchainContext *vulkanSwapchainContext, VkRenderPass renderPass) {
    // Allocate memory for framebuffers
    VkFramebuffer *swapChainFramebuffers = malloc(sizeof(VkFramebuffer) * vulkanSwapchainContext->swapChainImageCount);
    if (swapChainFramebuffers == NULL) {
        fprintf(stderr, "Failed to allocate memory for frame buffers\n");
        return NULL;
    }

    // Create a framebuffer for each swap chain image view
    for (size_t i = 0; i < vulkanSwapchainContext->swapChainImageCount; i++) {
        VkImageView attachments[2] = {
                vulkanSwapchainContext->swapChainImageViews[i], // Color attachment
                vulkanSwapchainContext->depthStencil->imageView          // Depth attachment
        };

        VkFramebufferCreateInfo framebufferInfo = {};
        framebufferInfo.sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
        framebufferInfo.renderPass = renderPass;
        framebufferInfo.attachmentCount = 2; // Now using 2 attachments: color and depth
        framebufferInfo.pAttachments = attachments;
        framebufferInfo.width = vulkanSwapchainContext->swapChainExtent.width;
        framebufferInfo.height = vulkanSwapchainContext->swapChainExtent.height;
        framebufferInfo.layers = 1;

        if (vkCreateFramebuffer(device, &framebufferInfo, NULL, &swapChainFramebuffers[i]) != VK_SUCCESS) {
            fprintf(stderr, "Failed to create framebuffer\n");
            // Clean up any framebuffers that were successfully created
            for (size_t j = 0; j < i; j++) {
                vkDestroyFramebuffer(device, swapChainFramebuffers[j], NULL);
            }
            free(swapChainFramebuffers);
            return VK_NULL_HANDLE;
        }
    }

    return swapChainFramebuffers;
}

VkFramebuffer createOffscreenFramebuffer(VkDevice device, VkImageView offscreenImageView, VkRenderPass renderPass, uint32_t width, uint32_t height) {
    VkImageView attachments[] = {
            offscreenImageView, // Off-screen color attachment
            // No depth attachment if not needed for your post-processing
    };

    VkFramebufferCreateInfo framebufferInfo = {};
    framebufferInfo.sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
    framebufferInfo.renderPass = renderPass; // Ensure this render pass is compatible with off-screen usage
    framebufferInfo.attachmentCount = 1; // Only one attachment for color
    framebufferInfo.pAttachments = attachments;
    framebufferInfo.width = width; // Width of your off-screen image
    framebufferInfo.height = height; // Height of your off-screen image
    framebufferInfo.layers = 1;

    VkFramebuffer offscreenFramebuffer;
    if (vkCreateFramebuffer(device, &framebufferInfo, NULL, &offscreenFramebuffer) != VK_SUCCESS) {
        fprintf(stderr, "Failed to create offscreen framebuffer\n");
        return VK_NULL_HANDLE; // Handle error appropriately
    }

    return offscreenFramebuffer;
}

