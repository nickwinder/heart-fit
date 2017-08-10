package com.nfx.android.heartfit

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.nfx.android.heartfit.GoogleFitManager.ConnectionStatus
import com.nfx.android.heartfit.dependancyinjection.BaseActivity
import com.nfx.android.heartfit.network.GoogleFitHeartRateInterface
import com.nfx.android.heartfit.network.HeartRateDataInterface
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class LoginScreen : BaseActivity() {
    @Inject lateinit var heartRateDataInterface: HeartRateDataInterface
    var googleFitConnectionListener: Disposable? = null

    private var authInProgress = false
    private val REQUEST_OAUTH = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)

        setContentView(R.layout.activity_login_screen)

        connectToHeartRateInterface()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (googleFitConnectionListener != null) {
            (googleFitConnectionListener as Disposable).dispose()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false
            if (resultCode == RESULT_OK) {
                (heartRateDataInterface as GoogleFitHeartRateInterface).connectToManager()
            } else if (resultCode == RESULT_CANCELED) {
                Log.e("GoogleFit", "RESULT_CANCELED")
            }
        } else {
            Log.e("GoogleFit", "requestCode NOT request_oauth")
        }
    }

    private fun connectToHeartRateInterface() {
        if (heartRateDataInterface is GoogleFitHeartRateInterface) {
            googleFitConnectionListener = (heartRateDataInterface as GoogleFitHeartRateInterface).getConnectionListener()
                    .subscribe { (connectionStatus, _, connectionResult) ->
                        when (connectionStatus) {
                            ConnectionStatus.CONNECTED -> progressToMainScreen()
                            ConnectionStatus.SUSPENDED -> googleFitAccessSuspended()
                            ConnectionStatus.DISCONNECTED -> handleReconnect(connectionResult)
                        }
                    }
        } else {
            progressToMainScreen()
        }
    }

    private fun progressToMainScreen() {
        val intent = Intent(this, HeartRateGraph::class.java)
        startActivity(intent)
    }

    private fun googleFitAccessSuspended() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun handleReconnect(connectionResult: ConnectionResult?) {
        if (connectionResult != null) {
            if (connectionResult.hasResolution()) {
                if (!authInProgress) {
                    try {
                        authInProgress = true
                        connectionResult.startResolutionForResult(this, REQUEST_OAUTH)
                    } catch(e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                    }
                } else {
                    Log.e("GoogleFit", "authInProgress")
                }
            }
        }
    }
}
