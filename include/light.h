#ifndef APP_LIGHT_H
#define APP_LIGHT_H

#include "cglm/cglm.h"
#include "model.h"

typedef struct {
    vec4 position;
    vec4 color;
    vec4 direction;
    int type;
    float intensity;
    float pad1;
    float pad2;
} Light;

Light *createLight(CreateLightInfo *info);
void destroyLight(Light *light);

#endif //APP_LIGHT_H
