#ifndef APP_UBO_H
#define APP_UBO_H

#include <cglm/cglm.h>
#include "include/light.h"

#define MAX_LIGHTS 5

typedef struct LightingUBO {
    Light lights[MAX_LIGHTS];
    vec4 ambientLightColor;
    vec4 cameraPos;
    int numLightsInUse; // should not exceed MAX_LIGHTS!
    float pad1;
    float pad2;
    float pad3;
} LightingUBO;

#endif
