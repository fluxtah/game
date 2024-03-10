#include <string.h>
#include "include/imagedata.h"
#include "libs/include/stb_image.h"

void readImageDataByPath(const char *filePath, ImageData *imageData) {
    int width, height, channels;
    // Load image resources with stbi_load
    unsigned char *loadedImage = stbi_load(filePath, &width, &height, &channels, STBI_rgb_alpha);
    if (loadedImage) {
        // Assign width, height, and size to your struct
        imageData->image_width = width;
        imageData->image_height = height;
        imageData->image_size = width * height * 4; // 4 bytes per pixel (RGBA)

        // Allocate memory for and copy imageData
        imageData->image_data = (uint8_t *) malloc(imageData->image_size);
        if (imageData->image_data != NULL) {
            memcpy(imageData->image_data, loadedImage, imageData->image_size);
        } else {
            printf("Failed to allocate image resources memory for the file");
            printf("Path: %s", filePath);
            stbi_image_free(loadedImage);
            exit(1);
        }

        // Free the loaded image resources
        stbi_image_free(loadedImage);
    } else {
        printf("Failed to load image resources from the file path");
        exit(1);
    }
}