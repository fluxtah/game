#include "include/entity.h"
#include "include/pipelines/ubo_transform.h"
#include "modules/physics/include/physics.h"

Entity *createEntity(ApplicationContext *context, CreateEntityInfo *info) {
    Entity *entity = malloc(sizeof(Entity));

    entity->useOBB = info->useOrientedBoundingBox;
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
    entity->obbs = malloc(sizeof(OBB) * entity->renderResources->num_aabbs);

    entity->num_aabbs = entity->renderResources->num_aabbs;
    for (int i = 0; i < entity->renderResources->num_aabbs; i++) {
        entity->aabbs[i] = entity->renderResources->aabbs[i];
    }

    if (entity->useOBB) {
        entity->num_obbs = entity->num_aabbs;
        updateEntityOBBs(entity);
    } else {
        updateEntityAABBs(entity);
    }

    return entity;
}

void initEntityPhysics(Entity *entity, void *physicsContext, bool isKinematic) {
    if (entity->physicsBody != NULL) {
        LOG_ERROR("Entity already has a physics body, remove it first");
        exit(EXIT_FAILURE);
    }

    entity->physicsBody = createPhysicsRigidBodyFromAABBs(
            physicsContext,
            entity->kotlinEntityInfo,
            entity->collisionGroup,
            entity->collisionMask,
            entity->aabbs,
            entity->num_aabbs,
            isKinematic ? 0 : entity->mass
    );

    if (isKinematic) {
        entity->isKinematic = true;
        makePhysicsRigidBodyKinematic(entity->physicsBody);
    }

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

bool are_matrices_identical(mat4 matA, mat4 matB) {
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            if (matA[i][j] != matB[i][j]) {
                return false;
            }
        }
    }
    return true;
}

void applyEntityChanges(Entity *entity) {
    mat4 previousModelMatrix;
    glm_mat4_copy(entity->modelMatrix, previousModelMatrix);
    glm_mat4_identity(entity->modelMatrix);

    // Apply rotation and translation first
    glm_translate(entity->modelMatrix, entity->position);
    glm_rotate(entity->modelMatrix, glm_rad(entity->rotation[0]), (vec3) {1.0f, 0.0f, 0.0f}); // X rotation
    glm_rotate(entity->modelMatrix, glm_rad(entity->rotation[1]), (vec3) {0.0f, 1.0f, 0.0f}); // Y rotation
    glm_rotate(entity->modelMatrix, glm_rad(entity->rotation[2]), (vec3) {0.0f, 0.0f, 1.0f}); // Z rotation

    // Then apply non-uniform scaling
    glm_scale(entity->modelMatrix, entity->scale);

    if (entity->useOBB) {
        updateEntityOBBs(entity);
    } else {
        updateEntityAABBs(entity);
    }

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

void updateEntityAABBs(Entity *entity) {
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

void updateEntityOBBs(Entity *entity) {
    if (entity->num_aabbs == 0) {
        printf("Entity %s, has no AABBs\n", entity->renderResources->filename);
        return;
    }
    if (entity->num_obbs == 0) {
        printf("Entity %s, has no OBBs\n", entity->renderResources->filename);
        return;
    }

    for (int i = 0; i < entity->num_aabbs; i++) {
        AABB aabb = entity->renderResources->aabbs[i];

        // Calculate the original center and extents of the AABB
        glm_vec3_add(aabb.min, aabb.max, entity->obbs[i].center);
        glm_vec3_scale(entity->obbs[i].center, 0.5f, entity->obbs[i].center);
        glm_vec3_sub(aabb.max, entity->obbs[i].center, entity->obbs[i].extents);

        // Create rotation-translation matrix
        mat4 rotTransMatrix;
        glm_mat4_identity(rotTransMatrix);

        // Translate to the position, including the original center of the AABB
        vec3 finalPosition;
        glm_vec3_add(entity->position, entity->obbs[i].center, finalPosition);
        glm_translate(rotTransMatrix, finalPosition);

        // Apply rotation
        glm_rotate(rotTransMatrix, glm_rad(entity->rotation[0]), (vec3) {1.0f, 0.0f, 0.0f}); // X rotation
        glm_rotate(rotTransMatrix, glm_rad(entity->rotation[1]), (vec3) {0.0f, 1.0f, 0.0f}); // Y rotation
        glm_rotate(rotTransMatrix, glm_rad(entity->rotation[2]), (vec3) {0.0f, 0.0f, 1.0f}); // Z rotation

        // Create a separate scaling matrix
        mat4 scaleMatrix;
        glm_mat4_identity(scaleMatrix);
        glm_scale(scaleMatrix, entity->scale);

        // Combine the scale matrix with the rotation-translation matrix
        mat4 transformMatrix;
        glm_mat4_mul(rotTransMatrix, scaleMatrix, transformMatrix);

        // Store the combined transformation matrix for rendering the OBB
        glm_mat4_copy(transformMatrix, entity->obbs[i].transform);
    }
}

AABB getEntityAABB(Entity *entity) {
    return entity->aabbs[0];
}

void attachKotlinEntityInfo(Entity *entity, void *kotlinEntityInfo) {
    entity->kotlinEntityInfo = kotlinEntityInfo;
}

CCollisionInfo getEntityCollisionInfo(Entity *entityA, Entity *entityB, int aabbIndexA, int aabbIndexB) {
    CCollisionInfo info;

    // TODO there is more than one AABB
    AABB a = entityA->aabbs[aabbIndexA];
    AABB b = entityB->aabbs[aabbIndexB];

    float overlapX1 = a.max[0] - b.min[0];
    float overlapX2 = b.max[0] - a.min[0];
    float penetrationX = fminf(overlapX1, overlapX2);
    if (penetrationX < 0) penetrationX = 0;

    float overlapY1 = a.max[1] - b.min[1];
    float overlapY2 = b.max[1] - a.min[1];
    float penetrationY = fminf(overlapY1, overlapY2);
    if (penetrationY < 0) penetrationY = 0;

    float overlapZ1 = a.max[2] - b.min[2];
    float overlapZ2 = b.max[2] - a.min[2];
    float penetrationZ = fminf(overlapZ1, overlapZ2);
    if (penetrationZ < 0) penetrationZ = 0;

    info.penetration[0] = penetrationX;
    info.penetration[1] = penetrationY;
    info.penetration[2] = penetrationZ;

    if (penetrationX < penetrationY && penetrationX < penetrationZ) {
        info.normal[0] = (overlapX1 < overlapX2) ? -1.0f : 1.0f;
        info.normal[1] = 0.0f;
        info.normal[2] = 0.0f;
    } else if (penetrationY < penetrationX && penetrationY < penetrationZ) {
        info.normal[0] = 0.0f;
        info.normal[1] = (overlapY1 < overlapY2) ? -1.0f : 1.0f;
        info.normal[2] = 0.0f;
    } else {
        info.normal[0] = 0.0f;
        info.normal[1] = 0.0f;
        info.normal[2] = (overlapZ1 < overlapZ2) ? -1.0f : 1.0f;
    }

//    printf("Entity A AABB: Min(%f, %f, %f), Max(%f, %f, %f)\n", a.min[0], a.min[1], a.min[2], a.max[0], a.max[1], a.max[2]);
//    printf("Entity B AABB: Min(%f, %f, %f), Max(%f, %f, %f)\n", b.min[0], b.min[1], b.min[2], b.max[0], b.max[1], b.max[2]);
//    printf("Penetration: %f, %f, %f\n", info.penetration[0], info.penetration[1], info.penetration[2]);
//    printf("Normal: %f, %f, %f\n", info.normal[0], info.normal[1], info.normal[2]);

    return info;
}

void destroyEntity(ApplicationContext *context, Entity *entity) {
    free(entity->aabbs);
    free(entity->obbs);

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

bool shouldEntitiesCollide(const Entity *entityA, const Entity *entityB) {
    // Entities with a group or mask of zero are considered non-collidable
    if (entityA->collisionGroup == 0 || entityB->collisionGroup == 0) {
        return false; // One or both entities are not set to collide
    }

    // Check if entity A's group is in entity B's collision mask and vice versa
    bool collisionAB = (entityA->collisionGroup & entityB->collisionMask) != 0;
    bool collisionBA = (entityB->collisionGroup & entityA->collisionMask) != 0;

    return collisionAB && collisionBA; // Only collide if both checks pass
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
