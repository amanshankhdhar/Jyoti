// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView

class SettingsActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_settings)

        val swSound = findViewById<Switch>(R.id.swSound)
        val swHaptic = findViewById<Switch>(R.id.swHaptic)
        val swShake = findViewById<Switch>(R.id.swShake)
        val swClap = findViewById<Switch>(R.id.swClap)
        val swDark = findViewById<Switch>(R.id.swDark)

        swSound.isChecked = Prefs.getB(this, "sound", true)
        swHaptic.isChecked = Prefs.getB(this, "haptic", true)
        swShake.isChecked = Prefs.getB(this, "shake", false)
        swClap.isChecked = Prefs.getB(this, "clap", false)
        swDark.isChecked = Prefs.getB(this, "dark", false)

        swSound.setOnCheckedChangeListener { _, on -> Prefs.setB(this, "sound", on) }
        swHaptic.setOnCheckedChangeListener { _, on -> Prefs.setB(this, "haptic", on) }
        swShake.setOnCheckedChangeListener { _, on -> Prefs.setB(this, "shake", on); syncService() }
        swClap.setOnCheckedChangeListener { _, on ->
            Prefs.setB(this, "clap", on)
            if (on && Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 55)
            syncService()
        }
        swDark.setOnCheckedChangeListener { _, on -> Prefs.setB(this, "dark", on); syncService() }

        findViewById<TextView>(R.id.txtVer).text = "Jyoti v2.0.0 — by Aman Shankhdhar Ji"
    }

    private fun syncService() {
        val any = Prefs.getB(this, "shake", false) ||
                Prefs.getB(this, "clap", false) ||
                Prefs.getB(this, "dark", false)
        if (any) startService(Intent(this, AutomationService::class.java))
        else stopService(Intent(this, AutomationService::class.java))
    }
}