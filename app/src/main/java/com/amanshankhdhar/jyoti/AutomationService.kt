// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import android.os.Build
import android.os.IBinder
import android.os.PowerManager

class AutomationService : Service() {
    private var sm: SensorManager? = null
    private var wake: PowerManager.WakeLock? = null
    private var rec: AudioRecord? = null
    private var clapThread: Thread? = null
    @Volatile private var runClap = false
    
    private var lastAcc = 0f; private var lastShakeTime = 0L
    private var lastClapTime = 0L
    private var lastDarkTime = 0L

    override fun onBind(i: Intent?) = null

    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (wake == null) wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "jyoti:auto")
        wake?.takeIf { !it.isHeld }?.acquire()
        
        startForegroundNotification()
        startSensors()
        startClap()
        return START_STICKY
    }

    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("jyoti_auto", "Background Sensors", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
        val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, "jyoti_auto") 
                else @Suppress("DEPRECATION") Notification.Builder(this)
        b.setSmallIcon(R.drawable.ic_tile_off)
         .setContentTitle("Jyoti Sensors Active")
         .setContentText("Listening for shake, clap, or darkness...")
         .setOngoing(true)
        startForeground(99, b.build())
    }

    private fun startSensors() {
        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sm?.registerListener(accLsn, sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)
        sm?.registerListener(lightLsn, sm?.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL)
    }

    private val accLsn = object : SensorEventListener {
        override fun onSensorChanged(e: SensorEvent) {
            if (!Prefs.getB(this@AutomationService, "shake", false)) return
            val x = e.values[0]; val y = e.values[1]; val z = e.values[2]
            val acc = Math.sqrt((x*x + y*y + z*z).toDouble()).toFloat()
            val delta = Math.abs(acc - lastAcc)
            lastAcc = acc
            
            val sens = Prefs.getI(this@AutomationService, "shakeSens", 50)
            val threshold = 80 - (sens * 0.6f).toFloat() 
            
            if (delta > threshold) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > 1500) {
                    lastShakeTime = now
                    TorchManager.toggle(this@AutomationService)
                }
            }
        }
        override fun onAccuracyChanged(s: Sensor?, a: Int) {}
    }

    private val lightLsn = object : SensorEventListener {
        override fun onSensorChanged(e: SensorEvent) {
            if (!Prefs.getB(this@AutomationService, "dark", false)) return
            val lux = e.values[0]
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            val screenOn = if (Build.VERSION.SDK_INT >= 20) pm.isInteractive else @Suppress("DEPRECATION") pm.isScreenOn
            
            if (lux < 5 && screenOn && !TorchManager.isOn) {
                val now = System.currentTimeMillis()
                if (now - lastDarkTime > 10000) { 
                    lastDarkTime = now
                    val i = Intent(this@AutomationService, SmartDarkActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                }
            }
        }
        override fun onAccuracyChanged(s: Sensor?, a: Int) {}
    }

    private fun startClap() {
        if (clapThread != null) return
        runClap = true
        clapThread = Thread {
            try {
                val bufSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                rec = AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufSize)
                rec?.startRecording()
                val buffer = ShortArray(bufSize / 2)
                while (runClap) {
                    rec?.read(buffer, 0, buffer.size)
                    if (!Prefs.getB(this, "clap", false)) continue
                    var max = 0
                    for (s in buffer) { val a = Math.abs(s.toInt()); if (a > max) max = a }
                    
                    val sens = Prefs.getI(this, "clapSens", 50)
                    val threshold = 5000 - (sens * 35).toInt() 
                    
                    if (max > threshold) {
                        val now = System.currentTimeMillis()
                        if (now - lastClapTime > 1500) {
                            lastClapTime = now
                            TorchManager.toggle(this)
                            Thread.sleep(500)
                        }
                    }
                }
            } catch (e: Exception) {}
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