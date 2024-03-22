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
    entity->rotation[0] = info->rotationX;
    entity->rotation[1] = info->rotationY;
    entity->rotation[2] = info->rotationZ;
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

void setEntityRotation(Entity *obj, float x, float y, float z) {
    obj->rotation[0] = x;
    obj->rotation[1] = y;
    obj->rotation[2] = z;
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
    mat4 previousModelMatrix;
    glm_mat4_copy(entity->modelMatrix, previousModelMatrix);
    glm_mat4_identity(entity->modelMatrix);

    // Apply rotation and translation first
    glm_translate(entity->modelMatrix, entity->position);

    mat4 rot_matrix;
    glm_euler_zyx(entity->rotation, rot_matrix);
    glm_mat4_mul(entity->modelMatrix, rot_matrix, entity->modelMatrix);
    glm_scale(entity->modelMatrix, entity->scale);

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

float getEntityRotationX(Entity *entity) {
    return entity->rotation[0];
}

float getEntityRotationY(Entity *entity) {
    return entity->rotation[1];
}

float getEntityRotationZ(Entity *entity) {
    return entity->rotation[2];
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
