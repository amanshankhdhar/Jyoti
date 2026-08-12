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
    fun tick(c: Context) {
        if (Prefs.getB(c, "haptic", true)) {
            val v = c.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26)
                v.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            else @Suppress("DEPRECATION") v.vibrate(40)
        }
        if (Prefs.getB(c, "sound", true)) {
            if (tone == null) tone = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)
            tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
        }
    }
}