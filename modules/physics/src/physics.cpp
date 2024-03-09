#include <iostream>
#include <btBulletDynamicsCommon.h> // Include the Bullet Physics Header

extern "C" {
void physicsInit() {
    std::cout << "Hello Physics!" << std::endl;

    // Bullet Physics initialization
    // Create the Bullet world
    auto collisionConfiguration = new btDefaultCollisionConfiguration();
    auto dispatcher = new btCollisionDispatcher(collisionConfiguration);
    auto overlappingPairCache = new btDbvtBroadphase();
    auto solver = new btSequentialImpulseConstraintSolver;
    auto dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfiguration);

    // Set the gravity for our world
    dynamicsWorld->setGravity(btVector3(0, -10, 0));

    std::cout << "Bullet Physics world created." << std::endl;

    // Cleanup in the reverse order of creation/initialization
    delete dynamicsWorld;
    delete solver;
    delete overlappingPairCache;
    delete dispatcher;
    delete collisionConfiguration;

    std::cout << "Bullet Physics world deleted." << std::endl;
}
}
