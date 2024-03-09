#ifndef GAME_UBO_TRANSFORM_H
#define GAME_UBO_TRANSFORM_H

#include <cglm/cglm.h>

typedef struct TransformUBO {
    mat4 model;
    mat4 view;
    mat4 proj;
    vec3 cameraPos;
} TransformUBO;

#endif
