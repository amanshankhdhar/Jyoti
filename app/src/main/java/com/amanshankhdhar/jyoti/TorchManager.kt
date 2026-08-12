// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper

enum class TorchResult { ON, OFF, NO_HARDWARE, BLOCKED, FAILED }

object TorchManager {
    var isOn = false; private set
    var mode = "NORMAL"; private set
    var listener: ((Boolean) -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())
    private var step: Runnable? = null

    private fun cam(c: Context) = c.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private fun torchId(c: Context): String? =
        cam(c).cameraIdList.firstOrNull {
            cam(c).getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

    private fun apply(c: Context, on: Boolean) {
        val id = torchId(c)
        if (id == null) { isOn = false; return }
        isOn = on
        try {
            cam(c).setTorchMode(id, on)
            Notifier.update(c, on)
            listener?.invoke(on)
        } catch (e: Exception) {
            isOn = false
        }
    }

    fun toggle(c: Context): TorchResult {
        stopEffects(c)
        if (torchId(c) == null) return TorchResult.NO_HARDWARE
        return try {
            apply(c, !isOn)
            Feedback.tick(c)
            if (isOn) TorchResult.ON else TorchResult.OFF
        } catch (e: SecurityException) { isOn = false; TorchResult.BLOCKED }
        catch (e: Exception) { isOn = false; TorchResult.FAILED }
    }

    fun forceOff(c: Context) {
        stopEffects(c)
        if (isOn) apply(c, false)
    }

    private val strengthKey by lazy {
        try { CameraCharacteristics::class.java.getField("FLASH_INFO_STRENGTH_MAXIMUM_LEVEL").get(null) }
        catch (e: Exception) { null }
    }
    fun maxStrength(c: Context): Int {
        val k = strengthKey ?: return 0
        return try {
            @Suppress("UNCHECKED_CAST")
            cam(c).getCameraCharacteristics(torchId(c)!!).get(k as CameraCharacteristics.Key<Int>) ?: 0
        } catch (e: Exception) { 0 }
    }
    fun setStrength(c: Context, level: Int): Boolean = try {
        val m = CameraManager::class.java.getMethod("turnOnTorchWithStrengthLevel", String::class.java, Int::class.java)
        m.invoke(cam(c), torchId(c), level); true
    } catch (e: Exception) { false }

    fun startStrobe(c: Context, hz: Int) {
        stopEffects(c); mode = "STROBE"
        val period = 1000L / hz.coerceAtLeast(1)
        step = object : Runnable { override fun run() { apply(c, !isOn); handler.postDelayed(this, period) } }
            .also { handler.post(it) }
    }

    fun startSOS(c: Context) {
        stopEffects(c); mode = "SOS"
        val seq = mutableListOf<Pair<Boolean, Long>>()
        val D = 200L; val DA = 600L; val G = 200L; val LG = 600L; val END = 1400L
        repeat(3) { seq.add(true to D); seq.add(false to G) }; seq.add(false to LG - G)
        repeat(3) { seq.add(true to DA); seq.add(false to G) }; seq.add(false to LG - G)
        repeat(3) { seq.add(true to D); seq.add(false to G) }; seq.add(false to END)
        var i = 0
        step = object : Runnable { override fun run() {
            val s = seq[i]; apply(c, s.first); i = (i + 1) % seq.size
            handler.postDelayed(this, s.second)
        } }.also { handler.post(it) }
    }

    fun stopEffects(c: Context) {
        step?.let { handler.removeCallbacks(it) }; step = null
        if (mode != "NORMAL") { mode = "NORMAL"; if (isOn) apply(c, false) }
    }
}