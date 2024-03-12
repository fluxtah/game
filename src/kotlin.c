#include "include/kotlin.h"
#include "include/vulkan/setup.h"
#include "include/emitter.h"

void setActiveCamera(ApplicationContext *context, Camera *camera) {
    context->activeCamera = camera;

    // Default to extent
    if (camera->aspectRatio == 0.0f) {
        camera->aspectRatio = (float) context->vulkanSwapchainContext->swapChainExtent.width /
                              (float) context->vulkanSwapchainContext->swapChainExtent.height;
        applyCameraChanges(camera);
    }
}

int isKeyPressed(int key) {
    if (keys[key]) {
        return 1;
    }

    return -1;
}

void key_callback(GLFWwindow *window, int key, int scancode, int action, int mods) {
    if (key >= 0 && key < 1024) {
        if (action == GLFW_PRESS) {
            keys[key] = true;
        } else if (action == GLFW_RELEASE) {
            keys[key] = false;
        }
    }
}

void bindKotlinApi() {
    // Application
    ktSetEnableDebugBoundingVolumesFunc(enableDebugBoundingVolumes);

    // Input
    ktSetIsKeyPressedFunc(isKeyPressed);

    // Camera
    ktSetCreateCameraFunc(createCamera);
    ktSetDestroyCameraFunc(destroyCamera);
    ktSetMoveCameraForwardFunc(moveCameraForward);
    ktSetMoveCameraBackwardFunc(moveCameraBackward);
    ktSetMoveCameraLeftFunc(moveCameraLeft);
    ktSetMoveCameraRightFunc(moveCameraRight);
    ktSetPitchCameraFunc(pitchCamera);
    ktSetYawCameraFunc(yawCamera);
    ktSetPositionCameraFunc(positionCamera);
    ktSetApplyCameraChangesFunc(applyCameraChanges);
    ktSetActiveCameraFunc(setActiveCamera);
    ktSetCameraLookAtFunc(setCameraLookAt);

    ktSetWorldToScreenPointFunc(worldToScreenPoint);

    // Light
    ktSetCreateLightFunc(createLight);
    ktSetDestroyLightFunc(destroyLight);

    // Entity
    ktSetCreateEntityFunc(createEntity);
    ktSetDestroyEntityFunc(destroyEntity);
    ktSetPositionEntityFunc(setEntityPosition);
    ktSetEntityRotationFunc(setEntityRotation);
    ktSetEntityScaleFunc(setEntityScale);
    ktSetEntityVelocityFunc(setEntityVelocity);
    ktSetEntityMassFunc(setEntityMass);

    ktGetEntityPositionXFunc(getEntityPositionX);
    ktGetEntityPositionYFunc(getEntityPositionY);
    ktGetEntityPositionZFunc(getEntityPositionZ);

    ktGetEntityRotationXFunc(getEntityRotationX);
    ktGetEntityRotationYFunc(getEntityRotationY);
    ktGetEntityRotationZFunc(getEntityRotationZ);

    ktGetEntityScaleXFunc(getEntityScaleX);
    ktGetEntityScaleYFunc(getEntityScaleY);
    ktGetEntityScaleZFunc(getEntityScaleZ);

    ktGetEntityVelocityXFunc(getEntityVelocityX);
    ktGetEntityVelocityYFunc(getEntityVelocityY);
    ktGetEntityVelocityZFunc(getEntityVelocityZ);
    ktGetEntityMassFunc(getEntityMass);

    ktSetAttachKotlinEntityFunc(attachKotlinEntityInfo);
    ktGetEntityCollisionInfoFunc(getEntityCollisionInfo);
    ktSetEntitySkinIndexFunc(setEntitySkinIndex);
    // ktGetEntityAabbFunc(getEntityAABB);

    // Emitter
    ktSetCreateEmitterFunc(createEmitter);
    ktSetDestroyEmitterFunc(destroyEmitter);
    ktSetPositionEmitterFunc(setEmitterPosition);
    ktSetEmitterRotationFunc(setEmitterRotation);
    ktSetEmitterScaleFunc(setEmitterScale);
    ktSetEmitterResetFunc(resetEmitter);
    ktSetEmitterSpawnRateFunc(setEmitterSpawnRate);
    ktSetEmitterLifetimeFunc(setEmitterLifeTime);

    // Sound
    ktSetLoadSoundFunc(loadSound);
    ktSetDestroySoundFunc(destroySound);
    ktSetPlaySoundFunc(playSound);
    ktSetIsSoundPlayingFunc(isSoundPlaying);
    ktSetStopSoundFunc(stopSound);
    ktSetSoundPitchFunc(setSoundPitch);
    ktSetSoundPositionFunc(setSoundPosition);

    // Sprite sheets
    ktSetCreateSpriteSheetFunc(createSpriteSheet);
    ktSetDestroySpriteSheetFunc(destroySpriteSheet);

    ktSetCreateTextBatchFunc(createTextBatch);
    ktSetDestroyTextBatchFunc(destroyTextBatch);
    ktSetInitializeTextBatchFunc(initializeTextBatch);
    ktSetCreateTextElementFunc(createTextElement);
    ktSetAddTextElementToBatchFunc(addTextElementToBatch);
    ktSetUpdateTextElementSegmentInBatchFunc(updateTextElementSegmentInBatch);
    ktSetUpdateTextElementPositionFunc(updateTextElementPosition);
    ktSetMeasureTextElementWidthFunc(measureTextElementWidth);
    ktSetMeasureTextElementHeightFunc(measureTextElementHeight);

    ktSetCreateSpriteBatchFunc(createSpriteBatch);
    ktSetDestroySpriteBatchFunc(destroySpriteBatch);
    ktSetInitializeSpriteBatchFunc(initializeSpriteBatch);
    ktSetCreateSpriteElementFunc(createSpriteElement);
    ktSetAddSpriteElementToBatchFunc(addSpriteElementToBatch);
    ktSetUpdateSpriteElementIndexInBatchFunc(updateSpriteElementIndexInBatch);
    ktSetTransformSpriteElementInBatchFunc(transformSpriteElementInBatch);
}


void NOOP_enableDebugBoundingVolumes(ApplicationContext *context, bool enable) {
    // NOOP
}

int NOOP_isKeyPressed(int key) {
    // NOOP
    return -1;
}

// TODO what functions are valid for headless mode?
// * Do we need collision detection?
// * Do we need to render anything?
// * Do we need to play sounds?
// * Do we need to handle input?
// * Do we need to create emitters?
void bindKotlinApiHeadless() {
    // Application
    ktSetEnableDebugBoundingVolumesFunc(NOOP_enableDebugBoundingVolumes);

    // Input
    ktSetIsKeyPressedFunc(NOOP_isKeyPressed);

    // Camera
    ktSetCreateCameraFunc(createCamera);
    ktSetDestroyCameraFunc(destroyCamera);
    ktSetMoveCameraForwardFunc(moveCameraForward);
    ktSetMoveCameraBackwardFunc(moveCameraBackward);
    ktSetMoveCameraLeftFunc(moveCameraLeft);
    ktSetMoveCameraRightFunc(moveCameraRight);
    ktSetPitchCameraFunc(pitchCamera);
    ktSetYawCameraFunc(yawCamera);
    ktSetPositionCameraFunc(positionCamera);
    ktSetApplyCameraChangesFunc(applyCameraChanges);
    ktSetActiveCameraFunc(setActiveCamera);
    ktSetCameraLookAtFunc(setCameraLookAt);

    ktSetWorldToScreenPointFunc(worldToScreenPoint);

    // Light
    ktSetCreateLightFunc(createLight);
    ktSetDestroyLightFunc(destroyLight);

    // Entity
    ktSetCreateEntityFunc(createEntity);
    ktSetDestroyEntityFunc(destroyEntity);
    ktSetPositionEntityFunc(setEntityPosition);
    ktSetEntityRotationFunc(setEntityRotation);
    ktSetEntityScaleFunc(setEntityScale);
    ktSetEntityVelocityFunc(setEntityVelocity);
    ktSetEntityMassFunc(setEntityMass);

    ktGetEntityPositionXFunc(getEntityPositionX);
    ktGetEntityPositionYFunc(getEntityPositionY);
    ktGetEntityPositionZFunc(getEntityPositionZ);

    ktGetEntityRotationXFunc(getEntityRotationX);
    ktGetEntityRotationYFunc(getEntityRotationY);
    ktGetEntityRotationZFunc(getEntityRotationZ);

    ktGetEntityScaleXFunc(getEntityScaleX);
    ktGetEntityScaleYFunc(getEntityScaleY);
    ktGetEntityScaleZFunc(getEntityScaleZ);

    ktGetEntityVelocityXFunc(getEntityVelocityX);
    ktGetEntityVelocityYFunc(getEntityVelocityY);
    ktGetEntityVelocityZFunc(getEntityVelocityZ);
    ktGetEntityMassFunc(getEntityMass);

    ktSetAttachKotlinEntityFunc(attachKotlinEntityInfo);
    ktGetEntityCollisionInfoFunc(getEntityCollisionInfo);
    ktSetEntitySkinIndexFunc(setEntitySkinIndex);
    // ktGetEntityAabbFunc(getEntityAABB);

    // Emitter
    ktSetCreateEmitterFunc(createEmitter);
    ktSetDestroyEmitterFunc(destroyEmitter);
    ktSetPositionEmitterFunc(setEmitterPosition);
    ktSetEmitterRotationFunc(setEmitterRotation);
    ktSetEmitterScaleFunc(setEmitterScale);
    ktSetEmitterResetFunc(resetEmitter);
    ktSetEmitterSpawnRateFunc(setEmitterSpawnRate);
    ktSetEmitterLifetimeFunc(setEmitterLifeTime);

    // Sound
    ktSetLoadSoundFunc(loadSound);
    ktSetDestroySoundFunc(destroySound);
    ktSetPlaySoundFunc(playSound);
    ktSetIsSoundPlayingFunc(isSoundPlaying);
    ktSetStopSoundFunc(stopSound);
    ktSetSoundPitchFunc(setSoundPitch);
    ktSetSoundPositionFunc(setSoundPosition);

    // Sprite sheets
    ktSetCreateSpriteSheetFunc(createSpriteSheet);
    ktSetDestroySpriteSheetFunc(destroySpriteSheet);

    ktSetCreateTextBatchFunc(createTextBatch);
    ktSetDestroyTextBatchFunc(destroyTextBatch);
    ktSetInitializeTextBatchFunc(initializeTextBatch);
    ktSetCreateTextElementFunc(createTextElement);
    ktSetAddTextElementToBatchFunc(addTextElementToBatch);
    ktSetUpdateTextElementSegmentInBatchFunc(updateTextElementSegmentInBatch);
    ktSetUpdateTextElementPositionFunc(updateTextElementPosition);
    ktSetMeasureTextElementWidthFunc(measureTextElementWidth);
    ktSetMeasureTextElementHeightFunc(measureTextElementHeight);

    ktSetCreateSpriteBatchFunc(createSpriteBatch);
    ktSetDestroySpriteBatchFunc(destroySpriteBatch);
    ktSetInitializeSpriteBatchFunc(initializeSpriteBatch);
    ktSetCreateSpriteElementFunc(createSpriteElement);
    ktSetAddSpriteElementToBatchFunc(addSpriteElementToBatch);
    ktSetUpdateSpriteElementIndexInBatchFunc(updateSpriteElementIndexInBatch);
    ktSetTransformSpriteElementInBatchFunc(transformSpriteElementInBatch);
}