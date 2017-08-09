package com.nfx.android.heartfit.GoogleFitManager

import com.google.android.gms.common.api.Status

/**
 * NFX Development
 * Created by nick on 7/31/17.
 */

class FitApiException(status: Status, cause: Throwable? = null): Exception(status.statusMessage, cause)