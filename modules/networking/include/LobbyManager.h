#ifndef GAME_LOBBY_MANAGER_H
#define GAME_LOBBY_MANAGER_H

#include <iostream>
#include "isteamuser.h"
#include "steam_api.h"
#include "lobby.h"

class LobbyManager {
public:
    LobbyManager() = default;

    ~LobbyManager() {
        ClearLobbies();
    }

    void LoadLobbies();

    void SetOnLobbyLoadedCallbackFunction(void (*callback)(CLobby *lobbies, int count)) {
        m_OnLobbyLoadedCallback = callback;
    }

private:
    void (*m_OnLobbyLoadedCallback)(CLobby *lobbies, int count) = nullptr;

    CCallResult<LobbyManager, LobbyMatchList_t> m_SteamCallResultLobbyMatchList;

    CLobby *m_Lobbies = nullptr;
    uint32_t m_NumLobbies = 0;

    bool m_bLoadingLobbies = false;

    void ClearLobbies() {
        // if we have a list of previous lobbies, delete them
        if (m_Lobbies) {
            delete[] m_Lobbies;
            m_Lobbies = nullptr;
            m_NumLobbies = 0;
        }
    }

    void OnLobbyMatchListCallback(LobbyMatchList_t *pLobbyMatchList, bool bIOFailure) {
        ClearLobbies();

        if (bIOFailure) {
            std::cerr << "Loading lobbies failed" << std::endl;
            return;
        }

        // print out the number of lobbies found
        std::cout << "Lobbies found: " << pLobbyMatchList->m_nLobbiesMatching << std::endl;

        // print out lobby names
        for (int i = 0; i < pLobbyMatchList->m_nLobbiesMatching; i++) {
            CSteamID lobbySteamID = SteamMatchmaking()->GetLobbyByIndex(i);
            const char *lobbyName = SteamMatchmaking()->GetLobbyData(lobbySteamID, "name");
            std::cout << "Lobby " << i << ": " << lobbyName << ", id: " << lobbySteamID.GetAccountID() << std::endl;
        }

        std::cout << "LobbyMatchList_t succeeded" << std::endl;

        m_bLoadingLobbies = false;

        if (m_OnLobbyLoadedCallback) {
            m_Lobbies = new CLobby[pLobbyMatchList->m_nLobbiesMatching];
            m_NumLobbies = pLobbyMatchList->m_nLobbiesMatching;

            for (int i = 0; i < pLobbyMatchList->m_nLobbiesMatching; i++) {
                CSteamID lobbySteamID = SteamMatchmaking()->GetLobbyByIndex(i);
                const char *lobbyName = SteamMatchmaking()->GetLobbyData(lobbySteamID, "name");
                m_Lobbies[i].id = lobbySteamID.GetAccountID();
                m_Lobbies[i].name = lobbyName;
                // ListLobbyData(lobbySteamID);
            }

            m_OnLobbyLoadedCallback(m_Lobbies, (int) m_NumLobbies);
        }
    }

    void ListLobbyData(CSteamID lobbyID) {
        int nData = SteamMatchmaking()->GetLobbyDataCount(lobbyID);
        char key[k_nMaxLobbyKeyLength];
        char value[k_cubChatMetadataMax];
        for (int i = 0; i < nData; ++i) {
            bool bSuccess = SteamMatchmaking()->GetLobbyDataByIndex(lobbyID, i, key, k_nMaxLobbyKeyLength, value,
                                                                    k_cubChatMetadataMax);
            if (bSuccess) {
                printf("Lobby Data %d, Key: \"%s\" - Value: \"%s\"\n", i, key, value);
            }
        }
    }
};

#endif //GAME_LOBBY_MANAGER_H
