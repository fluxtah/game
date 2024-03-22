#ifndef MODEL_H
#define MODEL_H

#include <stdlib.h>
#include <stdbool.h>

typedef struct Extent2D {
    uint32_t    width;
    uint32_t    height;
} Extent2D;

typedef struct Extent3D {
    uint32_t    width;
    uint32_t    height;
    uint32_t    depth;
} Extent3D;

typedef struct Offset2D {
    int32_t    x;
    int32_t    y;
} Offset2D;

typedef struct Offset3D {
    int32_t    x;
    int32_t    y;
    int32_t    z;
} Offset3D;

typedef struct Rect2D {
    Offset2D    offset;
    Extent2D    extent;
} Rect2D;

typedef struct Viewport {
    float    x;
    float    y;
    float    width;
    float    height;
    float    minDepth;
    float    maxDepth;
} Viewport;

typedef struct CreateCameraInfo {
    float positionX;
    float positionY;
    float positionZ;
    float    fov;
    float    aspect;
    float    near;
    float    far;
} CreateCameraInfo;

typedef struct CreateLightInfo {
    int type;
    float colorR;
    float colorG;
    float colorB;
    float colorA;
    float positionX;
    float positionY;
    float positionZ;
    float directionX;
    float directionY;
    float directionZ;
    float intensity;
} CreateLightInfo;

typedef struct CreateEntityInfo {
    char *modelFileName;
    float positionX;
    float positionY;
    float positionZ;
    float rotationX;
    float rotationY;
    float rotationZ;
    float scaleX;
    float scaleY;
    float scaleZ;
    float velocityX;
    float velocityY;
    float velocityZ;
    float mass;

    int collisionGroup;
    int collisionMask;

    int skinIndex;
} CreateEntityInfo;

typedef struct LightArray {
    void** lights; // Pointer to the first light
    int size;     // Size of the array
} LightArray;

typedef struct EntityArray {
    void** entities; // Pointer to the first entity
    int size;     // Size of the array
} EntityArray;

typedef struct CreateSoundInfo {
    int loop;
} CreateSoundInfo;


typedef struct CreateEmitterInfo {
    char *modelFileName;
    char *textureFileName;

    float emitterPositionX;
    float emitterPositionY;
    float emitterPositionZ;
    float emitterRotationX;
    float emitterRotationY;
    float emitterRotationZ;
    float emitterScaleX;
    float emitterScaleY;
    float emitterScaleZ;

    int maxParticles;
    int particleBatchSize;
    float particleLifetime;
    float particleSpawnRate;
    float particleGravityX;
    float particleGravityY;
    float particleGravityZ;

    float particleSpawnPositionFromX;
    float particleSpawnPositionFromY;
    float particleSpawnPositionFromZ;
    float particleSpawnPositionToX;
    float particleSpawnPositionToY;
    float particleSpawnPositionToZ;

    float particleSpawnVelocityFromX;
    float particleSpawnVelocityFromY;
    float particleSpawnVelocityFromZ;
    float particleSpawnVelocityToX;
    float particleSpawnVelocityToY;
    float particleSpawnVelocityToZ;

    char *computeShaderFileName;
    char *vertexShaderFileName;
    char *fragmentShaderFileName;

} CreateEmitterInfo;

typedef struct EmitterArray {
    void** emitters; // Pointer to the first entity
    int size;     // Size of the array
} EmitterArray;

typedef struct CCollisionInfo {
    float penetration[3]; // The penetration vector
    float normal[3]; // The normal of the collision
} CCollisionInfo;

typedef struct AABB {
    float min[3]; // Minimum corner of the AABB
    float max[3]; // Maximum corner of the AABB
} AABB;

typedef struct CreateSpriteSheetInfo {
    char *jsonFileName;
    char *textureFileName;
} CreateSpriteSheetInfo;

typedef struct CreateTextElementInfo {
    const char *textTemplate;
    const char **replacements;
    long numReplacements;
    float positionX;
    float positionY;
    float colorR;
    float colorG;
    float colorB;
    float colorA;
    float scale;
} CreateTextElementInfo;

typedef struct TextBatchArray {
    void** batches; // Pointer to the text batches
    int size;     // Size of the array
} TextBatchArray;

typedef struct CreateSpriteElementInfo {
    long spriteSheetIndex;
    float positionX;
    float positionY;
    float colorR;
    float colorG;
    float colorB;
    float colorA;
    float scale;
    float rotation;
    float startCrop;
    float endCrop;
} CreateSpriteElementInfo;

typedef struct SpriteBatchArray {
    void** batches; // Pointer to the sprite batches
    int size;     // Size of the array
} SpriteBatchArray;

typedef struct CBoundingVolumeCollisionResult {
    int sourceVolumeIndex;
    int targetVolumeIndex;
} CBoundingVolumeCollisionResult;

typedef struct CCollisionResult {
    void *sourceEntityInfo;
    void *targetEntityInfo;
    CBoundingVolumeCollisionResult results[100]; // Default to 100
    int numResults;
} CCollisionResult;

typedef struct CreatePhysicsInfo {
    float gravityX;
    float gravityY;
    float gravityZ;
} CreatePhysicsInfo;

typedef struct CCollisionContactPoint {
    float distance;
    float positionAX;
    float positionAY;
    float positionAZ;
    float positionBX;
    float positionBY;
    float positionBZ;
    float collisionNormalX;
    float collisionNormalY;
    float collisionNormalZ;
} CCollisionContactPoint;

typedef struct CCollisionResult2 {
    void *userPointerA;
    void *userPointerB;
    CCollisionContactPoint contactPoints[100]; // Default to 100
    int numContacts;
} CCollisionResult2;

#endif // MODEL_H