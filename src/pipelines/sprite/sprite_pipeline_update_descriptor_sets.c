#include "include/pipelines/sprite/sprite_pipeline_update_descriptor_sets.h"

void updateSpritePipelineDescriptorSets(
        VkDevice device,
        VkDescriptorSet fragmentDescriptorSet,
        VkImageView baseColorImageView,
        VkSampler sampler) {
    VkDescriptorImageInfo textureImageInfo = {};
    textureImageInfo.imageView = baseColorImageView;
    textureImageInfo.sampler = sampler;
    textureImageInfo.imageLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;

    VkWriteDescriptorSet descriptorWrite = {};
    descriptorWrite.sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
    descriptorWrite.dstSet = fragmentDescriptorSet;
    descriptorWrite.dstBinding = 0;
    descriptorWrite.dstArrayElement = 0;
    descriptorWrite.descriptorType = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
    descriptorWrite.descriptorCount = 1;
    descriptorWrite.pBufferInfo = NULL;
    descriptorWrite.pImageInfo = &textureImageInfo;

    VkWriteDescriptorSet sets[] = {descriptorWrite};

    vkUpdateDescriptorSets(device, 1, sets, 0, NULL);
}
