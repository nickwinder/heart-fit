package com.nfx.android.heartfit.dependancyinjection

import android.support.v7.app.AppCompatActivity
import com.nfx.android.heartfit.dependancyinjection.components.ActivityComponent
import com.nfx.android.heartfit.dependancyinjection.components.DaggerActivityComponent
import com.nfx.android.heartfit.dependancyinjection.module.ActivityModule

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
abstract class BaseActivity : AppCompatActivity() {
    var component: ActivityComponent =
        DaggerActivityComponent
                .builder()
                .activityModule(ActivityModule(this))
                .build()
}
