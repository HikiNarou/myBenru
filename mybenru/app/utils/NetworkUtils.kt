package com.mybenru.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

/**
 * Utility class for network operations
 */
object NetworkUtils {

    private val _isNetworkConnectedLiveData = MutableLiveData<Boolean>()
    val isNetworkConnectedLiveData: LiveData<Boolean> = _isNetworkConnectedLiveData

    /**
     * Check if the device is connected to the internet
     */
    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    )
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * Update network connection status
     */
    fun updateNetworkStatus(context: Context) {
        val isConnected = isNetworkConnected(context)
        _isNetworkConnectedLiveData.postValue(isConnected)
        Timber.d("Network status updated: ${if (isConnected) "Connected" else "Disconnected"}")
    }

    /**
     * NetworkMonitor untuk tracking network connection changes
     */
    class NetworkMonitor(private val context: Context) {
        private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        /**
         * Register network callback
         */
        fun registerNetworkCallback() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
            }
        }

        /**
         * Unregister network callback
         */
        fun unregisterNetworkCallback() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        }

        /**
         * Network callback for tracking network changes
         */
        private val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                _isNetworkConnectedLiveData.postValue(true)
                Timber.d("Network available")
            }

            override fun onLost(network: android.net.Network) {
                _isNetworkConnectedLiveData.postValue(false)
                Timber.d("Network lost")
            }
        }
    }
}