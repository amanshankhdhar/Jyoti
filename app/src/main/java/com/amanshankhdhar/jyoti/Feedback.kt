// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object Feedback {
    private var tone: ToneGenerator? = null
    private fun vib(c: Context) = c.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private fun beep() {
        if (tone == null) tone = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)
        tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
    }

    /** Heavy mechanical click — torch ON */
    fun heavy(c: Context) {
        if (Prefs.getB(c, "haptic", true)) {
            val v = vib(c)
            if (Build.VERSION.SDK_INT >= 26)
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 60), intArrayOf(0, 255, 0, 160), -1))
            else @Suppress("DEPRECATION") v.vibrate(longArrayOf(0, 30, 40, 60), -1) // Added -1 here
        }
        if (Prefs.getB(c, "sound", true)) beep()
    }

    /** Soft tick — torch OFF */
    fun soft(c: Context) {
        if (Prefs.getB(c, "haptic", true)) {
            val v = vib(c)
            if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(25, 80))
            else @Suppress("DEPRECATION") v.vibrate(25)
        }
    }

    /** Alert pulse — smart-dark countdown */
    fun alert(c: Context) {
        val v = vib(c)
        if (Build.VERSION.SDK_INT >= 26)
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 100, 60), -1))
        else @Suppress("DEPRECATION") v.vibrate(longArrayOf(0, 60, 100, 60), -1) // Added -1 here
        if (Prefs.getB(c, "sound", true)) beep()
    }

    fun tick(c: Context) = heavy(c)
}