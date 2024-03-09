#include "LobbyManager.h"

void LobbyManager::LoadLobbies() {
    if (m_bLoadingLobbies) {
        return;
    }

    // You can add filters to the lobby list request by doing
    // something like this, before calling RequestLobbyList:
    //   SteamMatchmaking()->AddRequestLobbyListStringFilter("name", "stnjsppk", k_ELobbyComparisonEqual);

    // request all lobbies for this game
    m_bLoadingLobbies = true;
    SteamAPICall_t hSteamAPICall = SteamMatchmaking()->RequestLobbyList();
    // set the function to call when this API call has completed
    m_SteamCallResultLobbyMatchList.Set(hSteamAPICall, this, &LobbyManager::OnLobbyMatchListCallback);
}

