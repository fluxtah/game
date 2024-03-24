#include "include/entity.h"
#include "include/pipelines/ubo_transform.h"
#include "modules/physics/include/physics.h"

Entity *createEntity(ApplicationContext *context, CreateEntityInfo *info) {
    Entity *entity = malloc(sizeof(Entity));

    entity->scale[0] = info->scaleX;
    entity->scale[1] = info->scaleY;
    entity->scale[2] = info->scaleZ;
    entity->position[0] = info->positionX;
    entity->position[1] = info->positionY;
    entity->position[2] = info->positionZ;
    entity->rotation[0] = info->rotationW;
    entity->rotation[1] = info->rotationX;
    entity->rotation[2] = info->rotationY;
    entity->rotation[3] = info->rotationZ;
    entity->velocity[0] = info->velocityX;
    entity->velocity[1] = info->velocityY;
    entity->velocity[2] = info->velocityZ;
    entity->mass = info->mass;

    entity->collisionGroup = info->collisionGroup;
    entity->collisionMask = info->collisionMask;
    entity->physicsBody = NULL; // This will be attached later
    entity->isKinematic = false; // This will be set later

    // Dynamically allocate a BufferMemory
    entity->transformUBO = (BufferMemory *) malloc(sizeof(BufferMemory));
    createBufferMemory(
            context->vulkanDeviceContext,
            entity->transformUBO,
            sizeof(TransformUBO),
            VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

    // Dynamically allocate a BufferMemory
    entity->lightingUBO = (BufferMemory *) malloc(sizeof(BufferMemory));
    createBufferMemory(context->vulkanDeviceContext, entity->lightingUBO, sizeof(LightingUBO),
                       VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                       VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

    entity->renderResources = createRenderResourcesFromFile(context, info->modelFileName);

    // Create descriptor sets
    if (allocateDescriptorSet(
            context->vulkanDeviceContext->device,
            context->pipelineConfig->descriptorPool,
            context->pipelineConfig->vertexShaderDescriptorSetLayout,
            &entity->vertexDescriptorSet) != VK_SUCCESS) {
        LOG_ERROR("Failed to allocate vertex descriptor set");
        return NULL;
    }

    if (allocateDescriptorSet(
            context->vulkanDeviceContext->device,
            context->pipelineConfig->descriptorPool,
            context->pipelineConfig->fragmentShaderDescriptorSetLayout,
            &entity->fragmentDescriptorSet) != VK_SUCCESS) {
        LOG_ERROR("Failed to allocate fragment descriptor set");
        return NULL;
    }

    updateBasicPipelineDescriptorSets(
            context->vulkanDeviceContext->device,
            entity->vertexDescriptorSet,
            entity->fragmentDescriptorSet,
            entity->transformUBO->buffer,
            entity->lightingUBO->buffer
    );


    entity->aabbs = malloc(sizeof(AABB) * entity->renderResources->num_aabbs);

    entity->num_aabbs = entity->renderResources->num_aabbs;
    for (int i = 0; i < entity->renderResources->num_aabbs; i++) {
        entity->aabbs[i] = entity->renderResources->aabbs[i];
    }


    return entity;
}

void initEntityPhysics(Entity *entity, void *physicsContext, bool isKinematic) {
    if (entity->physicsBody != NULL) {
        LOG_ERROR("Entity already has a physics body, remove it first");
        exit(EXIT_FAILURE);
    }

    entity->isKinematic = isKinematic;

    setupEntityAABBs(entity);

    entity->physicsBody = createPhysicsRigidBodyFromAABBs(
            physicsContext,
            entity->kotlinEntityInfo,
            entity->collisionGroup,
            entity->collisionMask,
            entity->aabbs,
            entity->num_aabbs,
            entity->mass,
            entity->isKinematic
    );

    updateEntityPhysicsTransform(entity);
}

void removeEntityPhysics(Entity *entity, void *physicsContext) {
    deletePhysicsRigidBody(physicsContext, entity->physicsBody);
    entity->physicsBody = NULL;
}

void setEntityPosition(Entity *obj, float x, float y, float z) {
    obj->position[0] = x;
    obj->position[1] = y;
    obj->position[2] = z;
}

void setEntityRotation(Entity *obj, float w, float x, float y, float z) {
    obj->rotation[0] = w;
    obj->rotation[1] = x;
    obj->rotation[2] = y;
    obj->rotation[3] = z;
}

void setEntityScale(Entity *obj, float x, float y, float z) {
    obj->scale[0] = x;
    obj->scale[1] = y;
    obj->scale[2] = z;
}

void setEntityVelocity(Entity *entity, float x, float y, float z) {
    entity->velocity[0] = x;
    entity->velocity[1] = y;
    entity->velocity[2] = z;
}

void setEntityMass(Entity *entity, float mass) {
    entity->mass = mass;
}

void applyEntityChanges(Entity *entity) {
    glm_mat4_identity(entity->modelMatrix);

    // Apply rotation and translation first
    glm_translate(entity->modelMatrix, entity->position);

    // Apply rotation
    vec4 rotation = {entity->rotation[1], entity->rotation[2], entity->rotation[3], entity->rotation[0]};
    glm_quat_rotate(entity->modelMatrix, rotation, entity->modelMatrix);
    glm_scale(entity->modelMatrix, entity->scale);

    // updateEntityAABBs(entity);

    if (entity->isKinematic || entity->mass == 0) {
        updateEntityPhysicsTransform(entity);
    }
}

void updateEntityPhysicsTransform(Entity *entity) {
    if (entity->physicsBody != NULL) {
        updatePhysicsRigidBodyTransform(
                entity->physicsBody,
                entity->position,
                entity->rotation,
                entity->velocity);
    }
}

void attachKotlinEntityInfo(Entity *entity, void *kotlinEntityInfo) {
    entity->kotlinEntityInfo = kotlinEntityInfo;
}

void destroyEntity(ApplicationContext *context, Entity *entity) {
    free(entity->aabbs);

    // Destroy UBOs
    destroyBufferMemory(context->vulkanDeviceContext, entity->transformUBO);
    destroyBufferMemory(context->vulkanDeviceContext, entity->lightingUBO);

    RenderResourcesMap *resources = getRenderResources(renderResourcesMap, entity->renderResources->filename);

    if (resources->refs == 1) {
        deleteRenderResources(&renderResourcesMap, resources);
        destroyRenderResources(context, entity->renderResources);
    } else {
        resources->refs--;
    }
}

void setEntitySkinIndex(Entity *entity, int skinIndex) {
    entity->skinIndex = skinIndex;
}

float getEntityPositionX(Entity *entity) {
    return entity->position[0];
}

float getEntityPositionY(Entity *entity) {
    return entity->position[1];
}

float getEntityPositionZ(Entity *entity) {
    return entity->position[2];
}

float getEntityRotationW(Entity *entity) {
    return entity->rotation[0];
}

float getEntityRotationX(Entity *entity) {
    return entity->rotation[1];
}

float getEntityRotationY(Entity *entity) {
    return entity->rotation[2];
}

float getEntityRotationZ(Entity *entity) {
    return entity->rotation[3];
}

float getEntityScaleX(Entity *entity) {
    return entity->scale[0];
}

float getEntityScaleY(Entity *entity) {
    return entity->scale[1];
}

float getEntityScaleZ(Entity *entity) {
    return entity->scale[2];
}

float getEntityVelocityX(Entity *entity) {
    return entity->velocity[0];
}

float getEntityVelocityY(Entity *entity) {
    return entity->velocity[1];
}

float getEntityVelocityZ(Entity *entity) {
    return entity->velocity[2];
}

float getEntityMass(Entity *entity) {
    return entity->mass;
}

void setupEntityAABBs(Entity *entity) {
    // Assuming original AABB is stored in the entities render resources
    for (int i = 0; i < entity->num_aabbs; i++) {
        AABB originalAABB = entity->renderResources->aabbs[i];

        // Scale the AABB
        vec3 scaledMin, scaledMax;
        glm_vec3_mul(originalAABB.min, entity->scale, scaledMin);
        glm_vec3_mul(originalAABB.max, entity->scale, scaledMax);

        // Translate the AABB
        glm_vec3_add(scaledMin, entity->position, entity->aabbs[i].min);
        glm_vec3_add(scaledMax, entity->position, entity->aabbs[i].max);
    }
}

float setEntityPhysicsActive(Entity *entity, bool active) {
    if (entity->physicsBody != NULL) {
        setPhysicsActive(entity->physicsBody, active);
    }
}
