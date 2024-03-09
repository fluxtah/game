#include <iostream>
#include "isteamuser.h"
#include "steam_api.h"
#include "LobbyManager.h"

#define USE_GS_AUTH_API

auto *lobbyManager = new LobbyManager();

extern "C" {
void OutputDebugString(const char *pchMsg) {
    fprintf(stderr, "%s", pchMsg);
}
void __cdecl SteamAPIDebugTextHook(int nSeverity, const char *pchDebugText) {
    // if you're running in the debugger, only warnings (nSeverity >= 1) will be sent
    // if you add -debug_steamapi to the command-line, a lot of extra informational messages will also be sent
    ::OutputDebugString(pchDebugText);

    if (nSeverity >= 1) {
        // place to set a breakpoint for catching API errors
        int x = 3;
        (void) x;
    }
}

void platformInit() {
    if (!SteamAPI_Init()) {
        std::cerr << "SteamAPI_Init failed" << std::endl;
        return;
    }

    SteamClient()->SetWarningMessageHook(&SteamAPIDebugTextHook);
}

void platformUserId() {
    std::cout << "Checking if user is logged in." << std::endl;
    if (SteamUser()->BLoggedOn()) {
        printf("User is logged in.\n");
        CSteamID id = SteamUser()->GetSteamID();
        std::cout << "User ID: " << id.GetAccountID() << std::endl;

        const char *playerName = SteamFriends()->GetPersonaName();
        std::cout << "Steam User Name: " << playerName << std::endl;
    } else {
        std::cout << "User not logged in." << std::endl;
    }

    std::cout << "Requesting lobby list..." << std::endl;
}

void platformRunCallbacks() {
    SteamAPI_RunCallbacks();
}

/**
 * Set the callback function to be called when the lobby list is received.
 *
 * @example
 * ```c
 * void onLobbyMatchListCallback(CLobby *lobbies, int count) {
 *    for (int i = 0; i < count; i++) {
 *    printf("Lobby %d: %s, id: %llu\n", i, lobbies[i].name, lobbies[i].id);
 *    }
 *    netPlatformFreeLobbies(lobbies);
 *    }
 *    platformSetOnLobbyMatchListCallbackFunction(onLobbyMatchListCallback);
 *    ```
 *
 * @param callback
 */
void platformSetOnLobbyMatchListCallbackFunction(void (*callback)(CLobby *lobbies, int count)) {
    lobbyManager->SetOnLobbyLoadedCallbackFunction(callback);
}

void platformFetchLobbies() {
    lobbyManager->LoadLobbies();
}

void platformCleanup() {
    delete lobbyManager;
    SteamAPI_Shutdown();
}
}
