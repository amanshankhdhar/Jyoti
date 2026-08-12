// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TorchResult { ON, OFF, NO_HARDWARE, BLOCKED, FAILED, LOW_BATTERY }

object TorchManager {
    var isOn = false; private set
    var mode = "NORMAL"; private set
    var listener: ((Boolean) -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())
    private var step: Runnable? = null
    private var onSince = 0L

    private fun cam(c: Context) = c.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private fun torchId(c: Context): String? =
        cam(c).cameraIdList.firstOrNull {
            cam(c).getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

    /* ---------- battery ---------- */
    fun batteryPct(c: Context): Int {
        val b = c.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return b?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    }
    fun batteryLow(c: Context) = batteryPct(c) in 0..15

    /* ---------- usage stats (for Dashboard) ---------- */
    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private fun bankUsage(c: Context, on: Boolean) {
        if (on) { onSince = System.currentTimeMillis(); return }
        if (onSince == 0L) return
        val sec = (System.currentTimeMillis() - onSince) / 1000
        onSince = 0L
        if (Prefs.getS(c, "statDate", "") != today()) { Prefs.setS(c, "statDate", today()); Prefs.setL(c, "statSec", 0) }
        Prefs.setL(c, "statSec", Prefs.getL(c, "statSec", 0) + sec)
    }
    fun usageToday(c: Context): Long {
        if (Prefs.getS(c, "statDate", "") != today()) return 0
        return Prefs.getL(c, "statSec", 0)
    }

    /* ---------- core ---------- */
    private fun apply(c: Context, on: Boolean) {
        val id = torchId(c)
        if (id == null) { isOn = false; return }
        if (on && batteryLow(c)) { isOn = false; listener?.invoke(false); return }
        isOn = on
        try {
            cam(c).setTorchMode(id, on)
            bankUsage(c, on)
            Notifier.update(c, on)
            listener?.invoke(on)
        } catch (e: Exception) { isOn = false }
    }

    fun toggle(c: Context): TorchResult {
        stopEffects(c)
        if (torchId(c) == null) return TorchResult.NO_HARDWARE
        if (!isOn && batteryLow(c)) return TorchResult.LOW_BATTERY
        apply(c, !isOn)
        if (isOn) Feedback.heavy(c) else Feedback.soft(c)
        return if (isOn) TorchResult.ON else TorchResult.OFF
    }

    fun forceOff(c: Context) { stopEffects(c); if (isOn) apply(c, false) }

    /* ---------- strength + memory ---------- */
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
        m.invoke(cam(c), torchId(c), level)
        Prefs.setI(c, "memStrength", level)
        true
    } catch (e: Exception) { false }

    /* ---------- effects ---------- */
    fun startStrobe(c: Context, hz: Int) {
        stopEffects(c); mode = "STROBE"; Prefs.setI(c, "memHz", hz)
        val period = 1000L / hz.coerceAtLeast(1)
        step = object : Runnable { override fun run() { apply(c, !isOn); handler.postDelayed(this, period) } }
            .also { handler.post(it) }
    }

    fun startSOS(c: Context) { stopEffects(c); mode = "SOS"; runPattern(c, textToMorsePattern("SOS")) }

    /* ---------- Text-to-Morse ---------- */
    private val MORSE = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".", 'F' to "..-.",
        'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
        'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.", 'Q' to "--.-", 'R' to ".-.",
        'S' to "...", 'T' to "-", 'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
        'Y' to "-.--", 'Z' to "--..", '0' to "-----", '1' to ".----", '2' to "..---",
        '3' to "...--", '4' to "....-", '5' to ".....", '6' to "-....", '7' to "--...",
        '8' to "---..", '9' to "----.", ' ' to "/"
    )

    fun textToMorsePattern(text: String): List<Pair<Boolean, Long>> {
        val seq = mutableListOf<Pair<Boolean, Long>>()
        val D = 150L; val DA = 450L; val GAP = 150L; val CH = 450L; val WORD = 1050L
        for (ch in text.uppercase()) {
            val code = MORSE[ch] ?: continue
            if (code == "/") { seq.add(false to WORD); continue }
            for (s in code) {
                seq.add(true to if (s == '.') D else DA)
                seq.add(false to GAP)
            }
            seq.add(false to CH - GAP)
        }
        return seq
    }

    fun flashMorse(c: Context, text: String) { stopEffects(c); mode = "MORSE"; runPattern(c, textToMorsePattern(text)) }

    private fun runPattern(c: Context, seq: List<Pair<Boolean, Long>>) {
        if (seq.isEmpty()) return
        var i = 0
        step = object : Runnable { override fun run() {
            val s = seq[i]
            try { cam(c).setTorchMode(torchId(c)!!, s.first); isOn = s.first; listener?.invoke(s.first) } catch (e: Exception) {}
            i = (i + 1) % seq.size
            handler.postDelayed(this, s.second)
        } }.also { handler.post(it) }
    }

    fun stopEffects(c: Context) {
        step?.let { handler.removeCallbacks(it) }; step = null
        if (mode != "NORMAL") { mode = "NORMAL"; if (isOn) apply(c, false) }
    }
}