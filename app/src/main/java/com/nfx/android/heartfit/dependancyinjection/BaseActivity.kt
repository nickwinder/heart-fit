package com.nfx.android.heartfit.dependancyinjection

import android.support.v7.app.AppCompatActivity
import com.nfx.android.heartfit.dependancyinjection.components.ActivityComponent
import com.nfx.android.heartfit.dependancyinjection.components.DaggerActivityComponent

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
abstract class BaseActivity : AppCompatActivity() {
    val component: ActivityComponent by lazy {
        DaggerActivityComponent
                .builder()
                .activity(this)
                .build()
    }
}
