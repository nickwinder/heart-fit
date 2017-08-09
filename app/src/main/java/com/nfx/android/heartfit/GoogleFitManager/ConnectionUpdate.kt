package com.nfx.android.heartfit.GoogleFitManager

import com.google.android.gms.common.ConnectionResult

/**
 * NFX Development
 * Created by nick on 7/31/17.
 */
class ConnectionUpdate {
    companion object {
        fun onConnected(): Connection {
            return Connection(connectionStatus = ConnectionStatus.CONNECTED, cause = null, connectionResult = null)
        }

        fun onSuspended(cause: Int): Connection {
            return Connection(connectionStatus = ConnectionStatus.SUSPENDED, cause = cause, connectionResult = null)
        }

        fun onFailed(result: ConnectionResult): Connection {
            return Connection(connectionStatus = ConnectionStatus.DISCONNECTED, connectionResult = result, cause = null)
        }
    }
}