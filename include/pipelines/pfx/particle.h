#ifndef APP_PIPELINES_PFX_PARTICLE_H
#define APP_PIPELINES_PFX_PARTICLE_H

#include "cglm/cglm.h"

typedef struct Particle {
    vec4 position;
    vec4 initialWorldPos;
    vec4 velocity;
    vec4 scale;
    vec4 color;
    float lifeTime;
    float spawnTime;
    float padding1;        // 4 bytes - added for alignment
    float padding2;        // 4 bytes - added for alignment
} Particle;

#endif //APP_PIPELINES_PFX_PARTICLE_H
