#include "lobby.h"

#ifdef __cplusplus
extern "C" {
#endif

void platformInit();
void platformUserId();
void platformRunCallbacks();
void platformSetOnLobbyMatchListCallbackFunction(void (*callback)(CLobby *lobbies, int count));
void platformFetchLobbies();
void platformCleanup();

#ifdef __cplusplus
}
#endif
