#include "include/sound.h"

SoundResourcesMap *soundResourcesMap;

void addSoundResources(SoundResourcesMap **hashmap, const char *filename, SoundResources *resources) {
    SoundResourcesMap *entry = NULL;
    HASH_FIND_STR(*hashmap, filename, entry);
    if (entry == NULL) {
        entry = (SoundResourcesMap *) malloc(sizeof(SoundResourcesMap));
        entry->filename = strdup(filename); // Duplicate the filename
        entry->resources = resources;
        entry->refs = 1;
        HASH_ADD_KEYPTR(hh, *hashmap, entry->filename, strlen(entry->filename), entry);
    }
}

SoundResourcesMap *getSoundResources(SoundResourcesMap *hashmap, const char *filename) {
    SoundResourcesMap *entry = NULL;
    HASH_FIND_STR(hashmap, filename, entry);
    return (entry != NULL) ? entry : NULL;
}

void deleteSoundResources(SoundResourcesMap **hashmap, SoundResourcesMap *entry) {
    HASH_DEL(*hashmap, entry);
    free(entry->filename);
    free(entry);
}

AudioContext *createAudioContext() {
    // Initialize OpenAL
    ALCdevice *device = alcOpenDevice(NULL);
    if (!device) {
        fprintf(stderr, "Error opening OpenAL device\n");
        return NULL;
    }

    ALCcontext *context = alcCreateContext(device, NULL);
    if (!context || !alcMakeContextCurrent(context)) {
        fprintf(stderr, "Error creating OpenAL context\n");
        alcCloseDevice(device);
        return NULL;
    }

    AudioContext *audioContext = malloc(sizeof(AudioContext));
    audioContext->device = device;
    audioContext->context = context;

    alDistanceModel(AL_LINEAR_DISTANCE);

    return audioContext;
}

void destroyAudioContext(AudioContext *context) {
    alcMakeContextCurrent(NULL);
    alcDestroyContext(context->context);
    alcCloseDevice(context->device);
}

SoundResources *createSoundResourcesFromFile(const char *filename) {
    SoundResourcesMap *existingData = getSoundResources(soundResourcesMap, filename);

    if (existingData != NULL) {
        existingData->refs++;
        return existingData->resources;
    }

    SoundResources *soundResources = malloc(sizeof(SoundResources));
    addSoundResources(&soundResourcesMap, filename, soundResources);

    soundResources->filename = strdup(filename);

    // Load WAV file
    drwav wav;
    if (!drwav_init_file(&wav, filename, NULL)) {
        fprintf(stderr, "Error opening WAV file\n");
        return NULL;
    }

    if (wav.bitsPerSample != 16) {
        fprintf(stderr, "Unsupported bit depth: %d\n", wav.bitsPerSample);
        drwav_uninit(&wav);
        return NULL;
    }

    size_t bufferSize = wav.totalPCMFrameCount * wav.channels * sizeof(short);
    short *pSampleData = (short *) malloc(bufferSize);
    if (pSampleData == NULL) {
        fprintf(stderr, "Error allocating memory\n");
        drwav_uninit(&wav);
        return NULL;
    }

    drwav_read_pcm_frames_s16(&wav, wav.totalPCMFrameCount, pSampleData);

    drwav_uninit(&wav);

    soundResources->data = pSampleData;
    soundResources->bufferSize = bufferSize;
    soundResources->sampleRate = wav.sampleRate;

    ALuint buffer;
    alGenBuffers(1, &buffer);
    ALenum format = (wav.channels == 1) ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
    alBufferData(buffer, format, soundResources->data, (ALsizei)soundResources->bufferSize, (ALsizei)soundResources->sampleRate);

    soundResources->buffer = buffer;

    return soundResources;
}

Sound *loadSound(const char *filename, CreateSoundInfo *info) {
    Sound *sound = malloc(sizeof(Sound));

    // Load WAV file
    sound->resources = createSoundResourcesFromFile(filename);

    ALuint source;
    alGenSources(1, &source);
    alSourcei(source, AL_BUFFER, (int)sound->resources->buffer);
    if(info->loop == 1) {
        alSourcei(source, AL_LOOPING, AL_TRUE);
    }
    sound->source = source;

    alSourcef(source, AL_MAX_DISTANCE, 200.0f);
    alSourcef(source, AL_ROLLOFF_FACTOR, 10.0f);

    return sound;
}

void destroySoundResources(SoundResources *resources) {
    alDeleteBuffers(1, &resources->buffer);
    free(resources->data);
    free(resources->filename);
    free(resources);
}
void destroySound(Sound *sound) {

    SoundResourcesMap *resources = getSoundResources(soundResourcesMap, sound->resources->filename);

    if (resources->refs == 1) {
        deleteSoundResources(&soundResourcesMap, resources);
        destroySoundResources(sound->resources);
    } else {
        resources->refs--;
    }

    alDeleteSources(1, &sound->source);
    free(sound);
}

void playSound(Sound *sound) {
    alSourcePlay(sound->source);
}

void stopSound(Sound *sound) {
    alSourceStop(sound->source);
}

int isSoundPlaying(Sound *sound) {
    ALint sourceState;
    alGetSourcei(sound->source, AL_SOURCE_STATE, &sourceState);
    if (sourceState == AL_PLAYING) {
        return 1;
    }

    return -1;
}

void setSoundPitch(Sound *sound, float pitch) {
    alSourcef(sound->source, AL_PITCH, pitch);
}

void setSoundPosition(Sound *sound, float x, float y, float z) {
    alSource3f(sound->source, AL_POSITION, x, y, z);
}

void setListenerPosition(float x, float y, float z) {
    alListener3f(AL_POSITION, x, y, z);
}
