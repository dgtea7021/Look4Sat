package com.rtbishop.look4sat.utility

import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.TimeUnit
import kotlin.math.round

object Utilities {

    fun String.snack(view: View) {
        Snackbar.make(view, this, Snackbar.LENGTH_SHORT).show()
    }

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    fun formatForTimer(millis: Long): String {
        val format = "%02d:%02d:%02d"
        val hours = TimeUnit.MILLISECONDS.toHours(millis) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format(format, hours, minutes, seconds)
    }
}