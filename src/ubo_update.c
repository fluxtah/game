#include "include/pipelines/ubo_update.h"
#include "include/pipelines/ubo_transform.h"
#include "include/pipelines/ubo_emitter.h"

void updateLightingUBO(VkDevice device, Entity *entity, Camera *camera) {
    LightArray *ktLights = (LightArray *) ktGetLights();

    if (!ktLights) return;

    Light lights[ktLights->size];

    for (int i = 0; i < ktLights->size; i++) {
        Light *light = (Light *) (ktLights->lights[i]);
        lights[i] = *light;
    }

    LightingUBO lightingUBO = {0};
    if(ktLights->size > MAX_LIGHTS) {
        lightingUBO.numLightsInUse = MAX_LIGHTS;
    } else {
        lightingUBO.numLightsInUse = ktLights->size;
    }

    memcpy(lightingUBO.lights, lights, sizeof(Light) * lightingUBO.numLightsInUse);

    vec4 camPos = {camera->position[0], camera->position[1], camera->position[2], 1.0f};
    glm_vec4_copy(camPos, lightingUBO.cameraPos);

    vec4 ambientLightColor = {0.01f, 0.01f, 0.01f, 1.0f};
    glm_vec4_copy(ambientLightColor, lightingUBO.ambientLightColor);

    void *lightsData;
    vkMapMemory(device, entity->lightingUBO->memory, 0, sizeof(LightingUBO), 0, &lightsData);
    memcpy(lightsData, &lightingUBO, sizeof(LightingUBO));
    vkUnmapMemory(device, entity->lightingUBO->memory);

    free(ktLights->lights);
    free(ktLights);
}

void updateTransformUBO(VkDevice device, Entity *entity, Camera *camera) {
    TransformUBO transformUBO = {0};
    glm_mat4_identity(transformUBO.model);

    // Apply rotation and translation first
    glm_translate(transformUBO.model, entity->position);
    glm_rotate(transformUBO.model, glm_rad(entity->rotation[0]), (vec3) {1.0f, 0.0f, 0.0f}); // X rotation
    glm_rotate(transformUBO.model, glm_rad(entity->rotation[1]), (vec3) {0.0f, 1.0f, 0.0f}); // Y rotation
    glm_rotate(transformUBO.model, glm_rad(entity->rotation[2]), (vec3) {0.0f, 0.0f, 1.0f}); // Z rotation

    // Then apply non-uniform scaling
    glm_scale(transformUBO.model, entity->scale);

    memcpy(transformUBO.view, camera->view, sizeof(mat4));
    memcpy(transformUBO.proj, camera->proj, sizeof(mat4));
    memcpy(transformUBO.model, entity->modelMatrix, sizeof(mat4));

    glm_vec3_copy(camera->position, transformUBO.cameraPos);

    void *transformData;
    vkMapMemory(device, entity->transformUBO->memory, 0, sizeof(TransformUBO), 0, &transformData);
    memcpy(transformData, &transformUBO, sizeof(TransformUBO));
    vkUnmapMemory(device, entity->transformUBO->memory);
}

void updateEmitterTransformUBO(VkDevice device, Emitter *emitter, Camera *camera) {
    TransformUBO transformUBO = {0};

    memcpy(transformUBO.view, camera->view, sizeof(mat4));
    memcpy(transformUBO.proj, camera->proj, sizeof(mat4));
    memcpy(transformUBO.model, emitter->modelMatrix, sizeof(mat4));

    glm_vec3_copy(camera->position, transformUBO.cameraPos);

    void *transformData;
    vkMapMemory(device, emitter->transformUBO->memory, 0, sizeof(TransformUBO), 0, &transformData);
    memcpy(transformData, &transformUBO, sizeof(TransformUBO));
    vkUnmapMemory(device, emitter->transformUBO->memory);
}

void updateEmitterParamsUBO(VkDevice device, Emitter *emitter) {
    EmitterParamsUBO paramsUBO = {
            .gravity = {emitter->gravity[0], emitter->gravity[1], emitter->gravity[2], emitter->gravity[3]},
            .spawnRate = emitter->spawnRate,
            .lifeTime = emitter->lifeTime,
            .spawnPositionFrom = {emitter->spawnPositionFrom[0], emitter->spawnPositionFrom[1], emitter->spawnPositionFrom[2], emitter->spawnPositionFrom[3]},
            .spawnPositionTo = {emitter->spawnPositionTo[0], emitter->spawnPositionTo[1], emitter->spawnPositionTo[2], emitter->spawnPositionTo[3]},
            .spawnVelocityFrom = {emitter->spawnVelocityFrom[0], emitter->spawnVelocityFrom[1], emitter->spawnVelocityFrom[2], emitter->spawnVelocityFrom[3]},
            .spawnVelocityTo = {emitter->spawnVelocityTo[0], emitter->spawnVelocityTo[1], emitter->spawnVelocityTo[2], emitter->spawnVelocityTo[3]},
    };

    void *params;
    vkMapMemory(device, emitter->computePipelineConfig->emitterParamsUBO->memory, 0, sizeof(EmitterParamsUBO), 0, &params);
    memcpy(params, &paramsUBO, sizeof(EmitterParamsUBO));
    vkUnmapMemory(device, emitter->computePipelineConfig->emitterParamsUBO->memory);
}

