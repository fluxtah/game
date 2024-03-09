package com.fluxtah.application.api.network

// Rough sketch of functions that would be needed for a network manager
interface NetworkManager {
    /**
     * Returns true if the current user is the host of the network game
     */
    fun isHost(): Boolean
    fun onUserConnected(user: NetworkUser)
    fun onUserDisconnected(user: NetworkUser)
    fun sendNetworkDataTo(user: NetworkUser, data: Any)
    fun broadcastNetworkData(data: Any)

    /**
     * Returns the data received from the network, each call to this function should return
     * the next piece of data until there is no more data to return in which case it should return null
     */
    fun <T> receiveNetworkData(): T?
}