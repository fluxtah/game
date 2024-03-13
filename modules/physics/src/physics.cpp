#include <iostream>
#include <btBulletDynamicsCommon.h> // Include the Bullet Physics Header
#include "../../../kotlin-sdk/cinterop/model.h"

extern "C" {

typedef struct PhysicsContext {
    btDefaultCollisionConfiguration *collisionConfiguration;
    btCollisionDispatcher *dispatcher;
    btBroadphaseInterface *overlappingPairCache;
    btSequentialImpulseConstraintSolver *solver;
    btDiscreteDynamicsWorld *dynamicsWorld;
} PhysicsContext;

void *initPhysics(CreatePhysicsInfo *info) {
    auto *context = new PhysicsContext();
    context->collisionConfiguration = new btDefaultCollisionConfiguration();
    context->dispatcher = new btCollisionDispatcher(context->collisionConfiguration);
    context->overlappingPairCache = new btDbvtBroadphase();
    context->solver = new btSequentialImpulseConstraintSolver;
    context->dynamicsWorld = new btDiscreteDynamicsWorld(context->dispatcher, context->overlappingPairCache, context->solver, context->collisionConfiguration);

    // Set the gravity for our world
    context->dynamicsWorld->setGravity(btVector3(info->gravityX, info->gravityY, info->gravityZ));

    std::cout << "Bullet Physics world created." << std::endl;
    return context;
}

void destroyPhysics(void *context) {
    auto *physicsContext = (PhysicsContext *) context;
    delete physicsContext->dynamicsWorld;
    delete physicsContext->solver;
    delete physicsContext->overlappingPairCache;
    delete physicsContext->dispatcher;
    delete physicsContext->collisionConfiguration;
    delete physicsContext;
    std::cout << "Bullet Physics world destroyed." << std::endl;
}
}