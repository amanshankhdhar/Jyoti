// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class SettingsActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_settings_v3)
        applyFonts(findViewById<ViewGroup>(android.R.id.content))

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
            if (on && Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 55)
            syncService()
        }
        swDark.setOnCheckedChangeListener { _, on -> Prefs.setB(this, "dark", on); syncService() }

        // Sliders
        val seekShake = findViewById<SeekBar>(R.id.seekShake)
        val seekClap = findViewById<SeekBar>(R.id.seekClap)
        val txtShake = findViewById<TextView>(R.id.txtShakeSens)
        val txtClap = findViewById<TextView>(R.id.txtClapSens)

        seekShake.progress = Prefs.getI(this, "shakeSens", 50)
        seekClap.progress = Prefs.getI(this, "clapSens", 50)
        updateSliderText(txtShake, seekShake.progress)
        updateSliderText(txtClap, seekClap.progress)

        seekShake.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) { Prefs.setI(this@SettingsActivity, "shakeSens", p); updateSliderText(txtShake, p) }
            override fun onStartTrackingTouch(s: SeekBar?) {}; override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        seekClap.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) { Prefs.setI(this@SettingsActivity, "clapSens", p); updateSliderText(txtClap, p) }
            override fun onStartTrackingTouch(s: SeekBar?) {}; override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        // GitHub Issues Button
        findViewById<Button>(R.id.btnIssue).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/amanshankhdhar/Jyoti/issues/new"))
            startActivity(intent)
        }

        findViewById<TextView>(R.id.txtVer).text = "Jyoti v${packageManager.getPackageInfo(packageName, 0).versionName}"
    }

    private fun updateSliderText(txt: TextView, progress: Int) {
        val label = when {
            progress < 33 -> "Low"
            progress < 66 -> "Medium"
            else -> "High"
        }
        txt.text = "Sensitivity: $label"
    }

    private fun syncService() {
        val any = Prefs.getB(this, "shake", false) || Prefs.getB(this, "clap", false) || Prefs.getB(this, "dark", false)
        if (any) startService(Intent(this, AutomationService::class.java))
        else stopService(Intent(this, AutomationService::class.java))
    }

    private fun applyFonts(v: View) {
        if (v is TextView) v.typeface = Fonts.regular(this)
        else if (v is ViewGroup) for (i in 0 until v.childCount) applyFonts(v.getChildAt(i))
    }
}