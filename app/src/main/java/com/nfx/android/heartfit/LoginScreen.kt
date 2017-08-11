package com.nfx.android.heartfit

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
    private val MY_PERMISSIONS_REQUEST_BODY_SENSORS = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)

        setContentView(R.layout.activity_login_screen)
    }

    override fun onStart() {
        super.onStart()

        connectToHeartRateInterface()
    }

    override fun onStop() {
        super.onStop()

        if (googleFitConnectionListener != null) {
            (googleFitConnectionListener as Disposable).dispose()
        }

        if (heartRateDataInterface is GoogleFitHeartRateInterface) {
            (heartRateDataInterface as GoogleFitHeartRateInterface).disconnectFromManager()
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
                            ConnectionStatus.CONNECTED -> checkPermissions()
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
        finish()
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

    private fun checkPermissions() {
        if (hasPermissions()) {
            progressToMainScreen()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                requestPermissions()
            }
        }
    }

    private fun hasPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.BODY_SENSORS),
                MY_PERMISSIONS_REQUEST_BODY_SENSORS)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_BODY_SENSORS -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    progressToMainScreen()
                } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.BODY_SENSORS)) {
                        permissionPermanentlyDenied()
                    } else {
                        showRationale()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    private fun showRationale() {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.permission_rationale))
                .setNeutralButton(android.R.string.ok, { _, _ -> requestPermissions() })
                .show()
    }

    private fun permissionPermanentlyDenied() {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.permission_rationale))
                .setPositiveButton(getString(R.string.permissions), { _, _ -> startApplicationDetailsActivity() })
                .setNegativeButton(android.R.string.cancel, { _, _ -> finish() })
                .show()
    }

    private fun startApplicationDetailsActivity() {
        try {
            //Open the specific App Info page:
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + packageName)
            startActivity(intent)

        } catch (e: ActivityNotFoundException) {
            //Open the generic Apps page:
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            startActivity(intent)
        }
        finish()
    }
}
