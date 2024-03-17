#include <iostream>
#include <btBulletDynamicsCommon.h> // Include the Bullet Physics Header
#include "../../../kotlin-sdk/cinterop/model.h"
#include "../../../libs/include/cglm/vec3.h"

typedef struct PhysicsContext {
    btDefaultCollisionConfiguration *collisionConfiguration;
    btCollisionDispatcher *dispatcher;
    btBroadphaseInterface *overlappingPairCache;
    btSequentialImpulseConstraintSolver *solver;
    btDiscreteDynamicsWorld *dynamicsWorld;

    void (*rigidBodyTransformUpdatedCallback)(void *, float, float, float, float, float, float);
} PhysicsContext;

void notifyRigidBodyTransformUpdated(PhysicsContext *physicsContext, btRigidBody *body, void *userPtr) {
    btTransform trans;
    body->getMotionState()->getWorldTransform(trans);
    // Convert btTransform's position to your entity's position format
    btVector3 pos = trans.getOrigin();

    btScalar roll, pitch, yaw;
    btQuaternion q = trans.getRotation();
    q.getEulerZYX(yaw, pitch, roll); // This gives yaw, pitch, and roll in radians

    float rotX = btDegrees(yaw);
    float rotY = btDegrees(pitch);
    float rotZ = btDegrees(roll);

    if (physicsContext->rigidBodyTransformUpdatedCallback != nullptr) {
        physicsContext->rigidBodyTransformUpdatedCallback(
                userPtr,
                pos.getX(), pos.getY(), pos.getZ(),
                rotX, rotY, rotZ);
    }
}

extern "C" {

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

    context->rigidBodyTransformUpdatedCallback = nullptr;

    std::cout << "Bullet Physics world created." << std::endl;

    return context;
}

void setOnRigidBodyUpdatedFunction(void *context,
                                   void (*callback)(void *userPtr,
                                                    float x, float y, float z,
                                                    float rotX, float rotY, float rotZ)) {
    auto *physicsContext = (PhysicsContext *) context;
    physicsContext->rigidBodyTransformUpdatedCallback = callback;
}

void checkKinematicRigidBodyCollisions(void *context) {
    auto *physicsContext = (PhysicsContext *) context;

    int numManifolds = physicsContext->dynamicsWorld->getDispatcher()->getNumManifolds();
    for (int i = 0; i < numManifolds; i++) {
        btPersistentManifold *contactManifold = physicsContext->dynamicsWorld->getDispatcher()->getManifoldByIndexInternal(
                i);
        const auto *obA = static_cast<const btCollisionObject *>(contactManifold->getBody0());
        const auto *obB = static_cast<const btCollisionObject *>(contactManifold->getBody1());

        // Determine if one object is kinematic and the other is not
        bool isKinematicA = obA->isKinematicObject();
        bool isKinematicB = obB->isKinematicObject();

        // Check for collisions between kinematic and (static or dynamic) objects
        if ((isKinematicA || isKinematicB)) {
            int numContacts = contactManifold->getNumContacts();
            for (int j = 0; j < numContacts; j++) {
                btManifoldPoint &pt = contactManifold->getContactPoint(j);
                if (pt.getDistance() < 0.f) {
                    std::cout << "Kinematic body collided with static/dynamic object:" << std::endl << pt.getDistance()
                              << std::endl;
                    // print collision info
                }
            }
        }
    }
}

void stepPhysicsSimulation(void *context, float timeStep) {
    auto *physicsContext = (PhysicsContext *) context;

    physicsContext->dynamicsWorld->stepSimulation(timeStep, 4);

    int numRigidBodies = physicsContext->dynamicsWorld->getNumCollisionObjects();
    for (int i = 0; i < numRigidBodies; i++) {
        btCollisionObject *obj = physicsContext->dynamicsWorld->getCollisionObjectArray()[i];
        btRigidBody *body = btRigidBody::upcast(obj);
        if (body && body->getMotionState()) {
            void *userPtr = body->getUserPointer();
            if (userPtr && body->isActive() && !body->isStaticOrKinematicObject()) {
                notifyRigidBodyTransformUpdated(physicsContext, body, userPtr);
            }
        }
    }

    checkKinematicRigidBodyCollisions(context);
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

void *
createPhysicsRigidBodyFromAABBs(void *context, void *data, int group, int mask, AABB *aabbs, int count, float mass) {
    auto *physicsContext = (PhysicsContext *) context;

    if (count == 0) {
        std::cerr << "No AABBs to add." << std::endl;
        return nullptr;
    }

    btCollisionShape *shape = nullptr;
    btMotionState *motionState = new btDefaultMotionState(); // Create a default motion state

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
    shape->calculateLocalInertia(mass, localInertia);

    btRigidBody::btRigidBodyConstructionInfo rbInfo(mass, motionState, shape,
                                                    localInertia); // Use 0 mass for static objects
    auto *body = new btRigidBody(rbInfo);
    // make extremely bouncey
    //   body->setRestitution(0.6f);
    // rotate extremely
//    body->setAngularFactor(btVector3(1, 1, 1));
//    body->applyTorqueImpulse(btVector3(10, 0, 2));
//    body->applyForce(btVector3(10, 0, 2), btVector3(0, 0, 0));

    body->setUserPointer(data);
    physicsContext->dynamicsWorld->addRigidBody(body, group, mask);

    return body;
}

void deletePhysicsRigidBody(void *context, void *body) {
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
    auto *rigidBody = (btRigidBody *) body;
    rigidBody->setCollisionFlags(rigidBody->getCollisionFlags() | btCollisionObject::CF_KINEMATIC_OBJECT);
    rigidBody->setActivationState(DISABLE_DEACTIVATION);
}

void updatePhysicsRigidBodyTransform(void *body, vec3 position, vec3 rotationDegrees, vec3 velocity) {
    auto *rigidBody = (btRigidBody *) body;
    // Convert degrees to radians for Bullet
    btVector3 btPosition(position[0], position[1], position[2]);
    btVector3 btVelocity(velocity[0], velocity[1], velocity[2]);

    btQuaternion btRotation;
    btRotation.setEulerZYX(
            btRadians(rotationDegrees[2]), // Yaw (Z rotation)
            btRadians(rotationDegrees[1]), // Pitch (Y rotation)
            btRadians(rotationDegrees[0]) // Roll (X rotation)
    );

    btTransform transform;
    transform.setIdentity();
    transform.setOrigin(btPosition);
    transform.setRotation(btRotation);

    rigidBody->setWorldTransform(transform);
    rigidBody->getMotionState()->setWorldTransform(transform);
    rigidBody->setLinearVelocity(btVelocity);
}

}