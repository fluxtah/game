#include <iostream>
#include <btBulletDynamicsCommon.h> // Include the Bullet Physics Header
#include "../../../kotlin-sdk/cinterop/model.h"
#include "../../../libs/include/cglm/vec3.h"
#include "BulletDebugDrawer.h"

typedef struct PhysicsContext {
    btDefaultCollisionConfiguration *collisionConfiguration;
    btCollisionDispatcher *dispatcher;
    btBroadphaseInterface *overlappingPairCache;
    btSequentialImpulseConstraintSolver *solver;
    btDiscreteDynamicsWorld *dynamicsWorld;
    BulletDebugDrawer *debugDrawer;

    void (*rigidBodyTransformUpdatedCallback)(void *, CPhysicsBodyUpdate update);

    void (*collisionCallback)(CCollisionResult2 *);
} PhysicsContext;

void notifyRigidBodyTransformUpdated(PhysicsContext *physicsContext, btRigidBody *body, void *userPtr) {
    if (physicsContext->rigidBodyTransformUpdatedCallback == nullptr) return;

    btTransform trans;
    body->getMotionState()->getWorldTransform(trans);
    btVector3 pos = trans.getOrigin();
    btQuaternion rot = trans.getRotation();

    CPhysicsBodyUpdate update;
    update.positionX = pos.getX();
    update.positionY = pos.getY();
    update.positionZ = pos.getZ();
    update.rotationW = rot.getW();
    update.rotationX = rot.getX();
    update.rotationY = rot.getY();
    update.rotationZ = rot.getZ();

    physicsContext->rigidBodyTransformUpdatedCallback(userPtr, update);
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
    context->collisionCallback = nullptr;

    context->debugDrawer = new BulletDebugDrawer();
    context->dynamicsWorld->setDebugDrawer(context->debugDrawer);

    std::cout << "Bullet Physics world created." << std::endl;

    return context;
}

void setOnRigidBodyUpdatedFunction(void *context, void (*callback)(void *userPtr, CPhysicsBodyUpdate update)) {
    auto *physicsContext = (PhysicsContext *) context;
    physicsContext->rigidBodyTransformUpdatedCallback = callback;
}

void setCollisionCallbackFunction(void *context, void (*callback)(CCollisionResult2 *result)) {
    auto *physicsContext = (PhysicsContext *) context;
    physicsContext->collisionCallback = callback;
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
        bool isStaticA = obA->isStaticObject();
        bool isStaticB = obB->isStaticObject();

        // Check for collisions between kinematic and (static or dynamic) objects
        int currentContactIndex = 0;
        if ((isKinematicA && isStaticB) || (isKinematicB && isStaticA) || (isKinematicA && isKinematicB)) {
            CCollisionResult2 result = {0};
            result.userPointerA = obA->getUserPointer();
            result.userPointerB = obB->getUserPointer();
            result.numContacts = contactManifold->getNumContacts();
            for (int j = 0; j < result.numContacts; j++) {
                btManifoldPoint &pt = contactManifold->getContactPoint(j);
                result.contactPoints[currentContactIndex].distance = pt.getDistance();

                result.contactPoints[currentContactIndex].positionAX = pt.getPositionWorldOnA().getX();
                result.contactPoints[currentContactIndex].positionAY = pt.getPositionWorldOnA().getY();
                result.contactPoints[currentContactIndex].positionAZ = pt.getPositionWorldOnA().getZ();
                result.contactPoints[currentContactIndex].positionBX = pt.getPositionWorldOnB().getX();
                result.contactPoints[currentContactIndex].positionBY = pt.getPositionWorldOnB().getY();
                result.contactPoints[currentContactIndex].positionBZ = pt.getPositionWorldOnB().getZ();
                result.contactPoints[currentContactIndex].collisionNormalX = pt.m_normalWorldOnB.getX();
                result.contactPoints[currentContactIndex].collisionNormalY = pt.m_normalWorldOnB.getY();
                result.contactPoints[currentContactIndex].collisionNormalZ = pt.m_normalWorldOnB.getZ();

                currentContactIndex++;
            }

            if (physicsContext->collisionCallback != nullptr) {
                physicsContext->collisionCallback(&result);
            }
        }
    }
}

void stepPhysicsSimulation(void *context, float timeStep) {
    auto *physicsContext = (PhysicsContext *) context;

    physicsContext->dynamicsWorld->stepSimulation(timeStep, 10);

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
    physicsContext->dynamicsWorld->debugDrawWorld();
}

void *getPhysicsDebugVertexData(void *context) {
    auto *physicsContext = (PhysicsContext *) context;
    return physicsContext->debugDrawer->getVertexData();
}

int getPhysicsDebugVertexCount(void *context) {
    auto *physicsContext = (PhysicsContext *) context;
    return physicsContext->debugDrawer->getVertexCount();
}

void destroyPhysics(void *context) {
    auto *physicsContext = (PhysicsContext *) context;
    delete physicsContext->dynamicsWorld;
    delete physicsContext->solver;
    delete physicsContext->overlappingPairCache;
    delete physicsContext->dispatcher;
    delete physicsContext->collisionConfiguration;
    delete physicsContext->debugDrawer;
    delete physicsContext;

    std::cout << "Bullet Physics world destroyed." << std::endl;
}

void *
createPhysicsRigidBodyFromAABBs(void *context, void *data, int group, int mask, AABB *aabbs, int count, float mass,
                                bool isKinematic) {
    auto *physicsContext = (PhysicsContext *) context;

    if (count == 0) {
        std::cerr << "No AABBs to add." << std::endl;
        return nullptr;
    }

    btCollisionShape *shape;
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
            btTransform trans = btTransform::getIdentity();
            trans.setOrigin(btVector3(
                    aabbs[i].translation[0],
                    aabbs[i].translation[1],
                    aabbs[i].translation[2]));
            compShape->addChildShape(trans, subShape);
        }
        shape = compShape;
    }

    btVector3 localInertia(0, 0, 0);
    shape->calculateLocalInertia(mass, localInertia);

    btRigidBody::btRigidBodyConstructionInfo rbInfo(mass, motionState, shape,
                                                    localInertia); // Use 0 mass for static objects
    auto *body = new btRigidBody(rbInfo);
    // TODO make it so we can set these from Kotlin
    // make extremely bouncey
    //   body->setRestitution(0.6f);
    // rotate extremely
//    body->setAngularFactor(btVector3(1, 1, 1));
//    body->applyTorqueImpulse(btVector3(10, 0, 2));
//    body->applyForce(btVector3(10, 0, 2), btVector3(0, 0, 0));

    if (isKinematic) {
        body->setCollisionFlags(body->getCollisionFlags() | btCollisionObject::CF_KINEMATIC_OBJECT);
        body->setActivationState(DISABLE_DEACTIVATION);
    }

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

void updatePhysicsRigidBodyTransform(void *body, vec3 position, versor rotation, vec3 velocity) {
    auto *rigidBody = (btRigidBody *) body;
    // Convert degrees to radians for Bullet
    btVector3 btPosition(position[0], position[1], position[2]);
    btVector3 btVelocity(velocity[0], velocity[1], velocity[2]);

    btQuaternion btRotation;
    btRotation.setW(rotation[0]);
    btRotation.setX(rotation[1]);
    btRotation.setY(rotation[2]);
    btRotation.setZ(rotation[3]);

    btTransform transform;
    transform.setIdentity();
    transform.setOrigin(btPosition);
    transform.setRotation(btRotation);

    rigidBody->setWorldTransform(transform);
    rigidBody->getMotionState()->setWorldTransform(transform);
    rigidBody->setLinearVelocity(btVelocity);
}

void setPhysicsActive(void *body, bool active) {
    auto *rigidBody = (btRigidBody *) body;
//    if (active) {
//        rigidBody->forceActivationState(ACTIVE_TAG);
//    } else {
//        rigidBody->forceActivationState(DISABLE_SIMULATION);
//    }
}

}