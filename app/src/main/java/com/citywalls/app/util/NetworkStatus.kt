package com.citywalls.app.util

import android.content.Context
import android.net.ConnectivityManager

/**
 * Connectivity probe backing the "download on Wi-Fi only" preference.
 * isActiveNetworkMetered covers API 16+, so no capability juggling is needed.
 */
class NetworkStatus(private val context: Context) {

    /** True when the active connection is metered (mobile data, tethering). */
    fun isMetered(): Boolean = runCatching {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.isActiveNetworkMetered
    }.getOrDefault(false)
}
