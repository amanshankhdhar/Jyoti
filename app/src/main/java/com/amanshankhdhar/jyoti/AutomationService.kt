// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.os.PowerManager

class AutomationService : Service() {
    private var sm: SensorManager? = null
    private var wake: PowerManager.WakeLock? = null
    private var rec: AudioRecord? = null
    private var clapThread: Thread? = null
    @Volatile private var runClap = false
    private var lastShake = 0L; private var lastClapToggle = 0L
    private var clapCount = 0; private var lastClapAt = 0L; private var darkAt = 0L

    override fun onBind(i: Intent?) = null

    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (wake == null) wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "jyoti:auto")
        wake?.takeIf { !it.isHeld }?.acquire()
        startSensors(); startClap()
        return START_STICKY
    }

    private fun startSensors() {
        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm?.registerListener(accLsn, sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)
        sm?.registerListener(lightLsn, sm?.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL)
    }

    private val accLsn = object : SensorEventListener {
        private var px = 0f; private var py = 0f; private var pz = 0f
        override fun onSensorChanged(e: SensorEvent) {
            if (!Prefs.getB(this@AutomationService, "shake", false)) return
            val x = e.values[0]; val y = e.values[1]; val z = e.values[2]
            val d = Math.abs(x - px) + Math.abs(y - py) + Math.abs(z - pz)
            px = x; py = y; pz = z
            val now = System.currentTimeMillis()
            if (d > 40 && now - lastShake > 2500) { lastShake = now; TorchManager.toggle(this@AutomationService) }
        }
        override fun onAccuracyChanged(s: Sensor?, a: Int) {}
    }

    private val lightLsn = object : SensorEventListener {
        override fun onSensorChanged(e: SensorEvent) {
            if (!Prefs.getB(this@AutomationService, "dark", false)) return
            val lux = e.values[0]; val now = System.currentTimeMillis()
            if (lux < 5 && !TorchManager.isOn && now - darkAt > 5000) {
                darkAt = now; TorchManager.toggle(this@AutomationService)
            }
        }
        override fun onAccuracyChanged(s: Sensor?, a: Int) {}
    }

    private fun startClap() {
        if (clapThread != null) return
        runClap = true
        clapThread = Thread {
            try {
                val n = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                rec = AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, n)
                rec?.startRecording()
                val buf = ShortArray(n / 2)
                while (runClap) {
                    rec?.read(buf, 0, buf.size)
                    if (!Prefs.getB(this, "clap", false)) continue
                    var max = 0
                    for (s in buf) { val a = Math.abs(s.toInt()); if (a > max) max = a }
                    val now = System.currentTimeMillis()
                    if (max > 3000) {
                        clapCount = if (now - lastClapAt < 1500) clapCount + 1 else 1
                        lastClapAt = now
                        if (clapCount >= 2 && now - lastClapToggle > 3000) {
                            lastClapToggle = now; clapCount = 0
                            TorchManager.toggle(this)
                        }
                        Thread.sleep(300)
                    }
                }
            } catch (e: Exception) { /* mic permission missing — clap stays off */ }
        }.also { it.start() }
    }

    override fun onDestroy() {
        runClap = false; clapThread?.interrupt()
        rec?.release(); rec = null
        sm?.unregisterListener(accLsn); sm?.unregisterListener(lightLsn)
        wake?.takeIf { it.isHeld }?.release()
        super.onDestroy()
    }
}
