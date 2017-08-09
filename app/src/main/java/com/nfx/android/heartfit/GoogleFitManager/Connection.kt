package com.nfx.android.heartfit.GoogleFitManager

import com.google.android.gms.common.ConnectionResult

/**
 * NFX Development
 * Created by nick on 7/31/17.
 */
data class Connection(var connectionStatus: ConnectionStatus, var cause: Int?, var connectionResult: ConnectionResult?)

enum class ConnectionStatus {
    CONNECTED, SUSPENDED, DISCONNECTED
}