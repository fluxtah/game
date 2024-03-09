#ifndef GAME_UBO_EMITTER_H
#define GAME_UBO_EMITTER_H

#include <cglm/cglm.h>

typedef struct EmitterParamsUBO {
    vec4 gravity;
    vec4 spawnVelocityFrom;
    vec4 spawnVelocityTo;
    vec4 spawnPositionFrom;
    vec4 spawnPositionTo;
    float spawnRate;
    float lifeTime;
} EmitterParamsUBO;
#endif //GAME_UBO_EMITTER_H
