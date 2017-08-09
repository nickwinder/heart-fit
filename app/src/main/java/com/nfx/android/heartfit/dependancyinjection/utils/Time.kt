package com.nfx.android.heartfit.dependancyinjection.utils

/**
 * NFX Development
 * Created by nick on 8/7/17.
 */
class Time {
    companion object {
        fun millisecondsToMinutes(millseconds: Long): Long {
            return millseconds / 60000
        }

        fun minutesToMilliseconds(minutes: Long): Long {
            return minutes * 60000
        }
    }
}