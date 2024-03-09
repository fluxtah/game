//
// Created by Ian Warwick on 27/01/2024.
//

#ifndef GAME_IMAGEDATA_H
#define GAME_IMAGEDATA_H

#include <stdint.h>

typedef struct ImageData {
    uint8_t* image_data;
    size_t image_size;
    int image_width;
    int image_height;
} ImageData;

void readImageDataByPath(const char *filePath, ImageData *imageData);
#endif //GAME_IMAGEDATA_H
