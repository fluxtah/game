#ifndef APP_SOUND_H
#define APP_SOUND_H

#include <stdio.h>
#include <stdlib.h>
#include <OpenAL/al.h>
#include <OpenAL/alc.h>
#include "libs/include/dr_wav.h"
#include "model.h"

#include "libs/include/uthash.h"

typedef struct SoundResources {
    char *filename;
    short *data;
    size_t bufferSize;
    uint32_t sampleRate;
    ALuint buffer;
} SoundResources;

typedef struct Sound {
    ALuint source;
    SoundResources *resources;
} Sound;

typedef struct AudioContext {
    ALCdevice *device;
    ALCcontext *context;
} AudioContext;

typedef struct SoundResourcesMap {
    char *filename;
    int refs;
    SoundResources *resources;
    UT_hash_handle hh;
} SoundResourcesMap;

extern SoundResourcesMap *soundResourcesMap;

AudioContext *createAudioContext();

void destroyAudioContext(AudioContext *context);

Sound *loadSound(const char *filename, CreateSoundInfo *info);
void destroySound(Sound *sound);

void playSound(Sound *sound);
int isSoundPlaying(Sound *sound);
void stopSound(Sound *sound);
void setSoundPitch(Sound *sound, float pitch);
void setSoundPosition(Sound *sound, float x, float y, float z);
void setListenerPosition(float x, float y, float z);
#endif //APP_SOUND_H
