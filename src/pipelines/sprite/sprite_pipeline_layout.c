#include "include/pipelines/sprite/sprite_pipeline_layout.h"

VkPipelineLayout createSpritePipelineLayout(
        VkDevice device,
        VkDescriptorSetLayout fragmentDescriptorSetLayout) {

    VkDescriptorSetLayout layouts[] = {fragmentDescriptorSetLayout};

    VkPushConstantRange pushConstantRange = {};
    pushConstantRange.stageFlags = VK_SHADER_STAGE_VERTEX_BIT; // Accessible in the vertex shader
    pushConstantRange.offset = 0;
    pushConstantRange.size = 192; // Size for model, view, proj matrices

    VkPipelineLayoutCreateInfo pipelineLayoutInfo = {};
    pipelineLayoutInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
    pipelineLayoutInfo.setLayoutCount = 1; // We have two descriptor set layouts
    pipelineLayoutInfo.pSetLayouts = layouts; // Pointer to descriptor set layout
    pipelineLayoutInfo.pushConstantRangeCount = 1;
    pipelineLayoutInfo.pPushConstantRanges = &pushConstantRange;


    VkPipelineLayout pipelineLayout;
    if (vkCreatePipelineLayout(device, &pipelineLayoutInfo, NULL, &pipelineLayout) != VK_SUCCESS) {
        LOG_ERROR("Failed to create basic pipeline layout");
        return VK_NULL_HANDLE;
    }

    return pipelineLayout;
}