package com.example.justfriends.Utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkManager(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun observeNetworkStatus(): Flow<Boolean> = callbackFlow {
        println("observe called")
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {

                super.onAvailable(network)
                println("on available called")
                trySend(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                println("on lost called")
                trySend(false)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        println("just registered")
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        awaitClose {
            println("should not deinit")
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}