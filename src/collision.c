#include "include/collision.h"
#include "include/entity.h"
#include "libkotlin_sdk_api.h"

void detectCollisions(const EntityArray *ktEntities) {
    for (size_t i = 0; i < ktEntities->size; i++) {

        Entity *sourceEntity = (Entity *) (ktEntities->entities[i]);
        for (size_t j = i + 1; j < ktEntities->size; j++) {
            Entity *otherEntity = (Entity *) (ktEntities->entities[j]);

            if (shouldEntitiesCollide(sourceEntity, otherEntity)) {
                int collidingVolumeCount = 0;
                CCollisionResult collisionResult = {0};
                collisionResult.sourceEntityInfo = sourceEntity->kotlinEntityInfo;
                collisionResult.targetEntityInfo = otherEntity->kotlinEntityInfo;

                for (int k = 0; k < sourceEntity->num_aabbs; k++) {
                    for (int l = 0; l < otherEntity->num_aabbs; l++) {
                        if (aabbCollision(&sourceEntity->aabbs[k], &otherEntity->aabbs[l])) {
                            //  printf("Collision detected between %d and %d\n", k, l);
                            collisionResult.results[collidingVolumeCount].sourceVolumeIndex = k;
                            collisionResult.results[collidingVolumeCount].targetVolumeIndex = l;
                            collidingVolumeCount++;
                        }
                    }
                }

                if (collidingVolumeCount > 0) {
                    collisionResult.numResults = collidingVolumeCount;
                    ktCollisionCallback(&collisionResult);
                }
            }
        }
    }
}