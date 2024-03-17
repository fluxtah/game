#ifndef APP_ENTITY_H
#define APP_ENTITY_H

#include <cglm/vec3.h>
#include <vulkan/vulkan.h>
#include "aabb.h"
#include "include/vulkan/buffer_memory.h"
#include "renderresources.h"
#include "obb.h"
#include "include/pipelines/basic/basic_pipeline_update_descriptor_sets.h"

typedef struct Entity {
    vec3 scale;
    vec3 position;
    vec3 rotation;
    vec3 velocity;
    float mass;

    mat4 modelMatrix;

    AABB *aabbs;
    int num_aabbs;

    OBB *obbs;
    int num_obbs;

    bool useOBB;   // Flag to indicate if the entity uses an OBB

    int collisionGroup;
    int collisionMask;

    VkDescriptorSet vertexDescriptorSet;
    VkDescriptorSet fragmentDescriptorSet;
    BufferMemory *transformUBO;
    BufferMemory *lightingUBO;
    RenderResources *renderResources;

    int skinIndex;

    void *kotlinEntityInfo;

    // Physics body pointer
    void *physicsBody;
} Entity;

RenderResourcesMap *renderResourcesMap;

Entity *createEntity(ApplicationContext *context, CreateEntityInfo *info);

void setEntityPosition(Entity *entity, float x, float y, float z);

void setEntityRotation(Entity *entity, float x, float y, float z);

void setEntityScale(Entity *obj, float x, float y, float z);

void setEntityVelocity(Entity *entity, float x, float y, float z);

void setEntityMass(Entity *entity, float mass);

void applyEntityChanges(Entity *entity);

void updateEntityPhysicsTransform(Entity *entity);

void updateEntityAABBs(Entity *entity);

void updateEntityOBBs(Entity *entity);

void attachKotlinEntityInfo(Entity *entity, void *kotlinEntityInfo);

void destroyEntity(ApplicationContext *context, Entity *entity);

CCollisionInfo getEntityCollisionInfo(Entity *entityA, Entity *entityB, int aabbIndexA, int aabbIndexB);

AABB getEntityAABB(Entity *entity);

bool shouldEntitiesCollide(const Entity *entityA, const Entity *entityB);

void setEntitySkinIndex(Entity *entity, int skinIndex);

float getEntityPositionX(Entity *entity);

float getEntityPositionY(Entity *entity);

float getEntityPositionZ(Entity *entity);

float getEntityRotationX(Entity *entity);

float getEntityRotationY(Entity *entity);

float getEntityRotationZ(Entity *entity);

float getEntityScaleX(Entity *entity);

float getEntityScaleY(Entity *entity);

float getEntityScaleZ(Entity *entity);

float getEntityVelocityX(Entity *entity);

float getEntityVelocityY(Entity *entity);

float getEntityVelocityZ(Entity *entity);

float getEntityMass(Entity *entity);

void initEntityPhysics(Entity *entity, void *physicsContext, bool isKinematic);

void removeEntityPhysics(Entity *entity, void *physicsContext);

#endif //APP_ENTITY_H
