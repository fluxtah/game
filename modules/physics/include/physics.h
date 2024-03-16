#ifdef __cplusplus
extern "C" {
#endif

void *initPhysics(CreatePhysicsInfo *info);

void destroyPhysics(void *context);

/**
 * Create a rigid body from an array of AABBs, will create a compound shape if there are more than one,
 * and add it to the physics world
 *
 * @param context The physics context
 * @param data A pointer to the data that will be associated with the body
 * @param group The collision group
 * @param mask The collision mask
 * @param aabbs The array of AABBs to create the rigid body from, will be a compound shape if there are more than one
 * @param count The number of AABBs
 * @return A pointer to the created rigid body
 */
void *createPhysicsRigidBodyFromAABBs(void *context, void *data, int group, int mask, AABB *aabbs, int count);

void updatePhysicsRigidBodyTransform(void *body, vec3 position, vec3 rotationDegrees, vec3 velocity, float mass);

void makePhysicsRigidBodyKinematic(void *body);

void setOnRigidBodyUpdatedFunction(void *context,
                                   void (*callback)(void *userPtr,
                                                    float x, float y, float z,
                                                    float rotX, float rotY, float rotZ));

void stepPhysicsSimulation(void *context, float timeStep);

void deletePhysicsRigidBody(void *context, void *body);
#ifdef __cplusplus
}
#endif
