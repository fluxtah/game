#include <iostream>
#include <btBulletDynamicsCommon.h> // Include the Bullet Physics Header
#include "../../../kotlin-sdk/cinterop/model.h"
#include "../../../libs/include/cglm/vec3.h"

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
    context->dynamicsWorld = new btDiscreteDynamicsWorld(context->dispatcher, context->overlappingPairCache,
                                                         context->solver, context->collisionConfiguration);

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

void *createPhysicsRigidBodyFromAABBs(void *context, void *data, int group, int mask, AABB *aabbs, int count) {
    std::cout << "Creating rigid body from AABBs." << std::endl;
    auto *physicsContext = (PhysicsContext *)context;

    if (count == 0) {
        std::cerr << "No AABBs to add." << std::endl;
        return nullptr;
    }

    btCollisionShape* shape = nullptr;
    btMotionState* motionState = new btDefaultMotionState(); // Create a default motion state

    if (count == 1) {
        shape = new btBoxShape(btVector3(
                (aabbs[0].max[0] - aabbs[0].min[0]) * 0.5f,
                (aabbs[0].max[1] - aabbs[0].min[1]) * 0.5f,
                (aabbs[0].max[2] - aabbs[0].min[2]) * 0.5f));
    } else {
        auto *compShape = new btCompoundShape();
        for (int i = 0; i < count; i++) {
            auto *subShape = new btBoxShape(btVector3(
                    (aabbs[i].max[0] - aabbs[i].min[0]) * 0.5f,
                    (aabbs[i].max[1] - aabbs[i].min[1]) * 0.5f,
                    (aabbs[i].max[2] - aabbs[i].min[2]) * 0.5f));
            compShape->addChildShape(btTransform::getIdentity(), subShape);
        }
        shape = compShape;
    }

    btVector3 localInertia(0, 0, 0);
    if (shape->isNonMoving()) {
        shape->calculateLocalInertia(0, localInertia); // Calculate inertia for non-moving objects, if necessary
    }

    btRigidBody::btRigidBodyConstructionInfo rbInfo(0, motionState, shape, localInertia); // Use 0 mass for static objects
    auto *body = new btRigidBody(rbInfo);
    body->setUserPointer(data);
    physicsContext->dynamicsWorld->addRigidBody(body, group, mask);

    return body;
}


void deletePhysicsRigidBody(void *context, void *body) {
    std::cout << "Deleting rigid body." << std::endl;

    auto *rigidBody = (btRigidBody *) body;
    auto *physicsContext = (PhysicsContext *) context;

    physicsContext->dynamicsWorld->removeRigidBody(rigidBody);

    btCollisionShape *shape = rigidBody->getCollisionShape();
    if (shape->getShapeType() == COMPOUND_SHAPE_PROXYTYPE) {
        auto *compoundShape = dynamic_cast<btCompoundShape *>(shape);
        for (int i = compoundShape->getNumChildShapes() - 1; i >= 0; i--) {
            btCollisionShape *childShape = compoundShape->getChildShape(i);
            // No need to removeChildShapeByIndex() if we're deleting the compound shape
            delete childShape;
        }
        delete compoundShape; // Delete the compound shape after its children
    } else {
        delete shape; // Directly delete if not a compound shape
    }

    // Proceed to delete the body and any other resources as usual
    delete rigidBody->getMotionState();
    delete rigidBody;

}

void makePhysicsRigidBodyKinematic(void *body) {
    std::cout << "Making rigid body kinematic." << std::endl;
    auto *rigidBody = (btRigidBody *) body;
    rigidBody->setCollisionFlags(rigidBody->getCollisionFlags() | btCollisionObject::CF_KINEMATIC_OBJECT);
    rigidBody->setActivationState(DISABLE_DEACTIVATION);
}

void updatePhysicsRigidBodyTransform(void *body, vec3 position, vec3 rotationDegrees, vec3 velocity, float mass) {
    auto *rigidBody = (btRigidBody *) body;
    btTransform transform;
    if (rigidBody->getMotionState() != nullptr) {
        rigidBody->getMotionState()->getWorldTransform(transform);
    }

    // Convert degrees to radians
    float rotationRadians[3];
    rotationRadians[0] = rotationDegrees[0] * btScalar(M_PI / 180.0);
    rotationRadians[1] = rotationDegrees[1] * btScalar(M_PI / 180.0);
    rotationRadians[2] = rotationDegrees[2] * btScalar(M_PI / 180.0);

    // Convert Euler angles (in radians) to a quaternion
    btQuaternion quaternion;
    quaternion.setEulerZYX(rotationRadians[2], rotationRadians[1], rotationRadians[0]); // ZYX order for Euler angles

    transform.setOrigin(btVector3(position[0], position[1], position[2]));
    transform.setRotation(quaternion);

    rigidBody->setWorldTransform(transform);
    if (rigidBody->getMotionState() != nullptr) {
        rigidBody->getMotionState()->setWorldTransform(transform);
    }
    rigidBody->setLinearVelocity(btVector3(velocity[0], velocity[1], velocity[2]));

    if (rigidBody->getMass() != mass) {
        // Recalculate inertia if mass is non-zero
        btVector3 inertia(0, 0, 0);
        if (mass != 0.f) {
            rigidBody->getCollisionShape()->calculateLocalInertia(mass, inertia);
        }
        rigidBody->setMassProps(mass, inertia);
        rigidBody->updateInertiaTensor(); // Always update inertia tensor after changing mass properties
    }
}

}