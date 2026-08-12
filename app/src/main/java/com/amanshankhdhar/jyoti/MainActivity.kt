// SPDX-License-Identifier: MIT
package com.amanshankhdhar.jyoti

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var badge: TextView
    private lateinit var power: ImageButton
    private lateinit var strength: SeekBar

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_home_v3)
        applyFonts(findViewById<ViewGroup>(android.R.id.content)) // Applies Poppins to everything!

        badge = findViewById(R.id.badge)
        power = findViewById(R.id.btnPower)
        strength = findViewById(R.id.seekStrength)

        TorchManager.listener = { runOnUiThread { refresh() } }

        power.setOnClickListener {
            when (val res = TorchManager.toggle(this)) {
                TorchResult.LOW_BATTERY -> Toast.makeText(this, "Battery < 15%. Torch blocked to save power.", Toast.LENGTH_LONG).show()
                TorchResult.BLOCKED -> Toast.makeText(this, "System blocked torch", Toast.LENGTH_LONG).show()
                TorchResult.NO_HARDWARE, TorchResult.FAILED -> Toast.makeText(this, "LED unavailable", Toast.LENGTH_LONG).show()
                else -> {}
            }
            refresh()
        }

        val max = TorchManager.maxStrength(this)
        if (max > 1) {
            strength.visibility = View.VISIBLE
            strength.max = max
            strength.progress = Prefs.getI(this, "memStrength", max)
            strength.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, from: Boolean) {
                    if (from && TorchManager.isOn) TorchManager.setStrength(this@MainActivity, p.coerceAtLeast(1))
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }

        findViewById<Button>(R.id.btnEffects).setOnClickListener { startActivity(Intent(this, EffectsActivity::class.java)) }
        findViewById<Button>(R.id.btnScreen).setOnClickListener { startActivity(Intent(this, ScreenLightActivity::class.java)) }
        findViewById<Button>(R.id.btnDashboard).setOnClickListener { startActivity(Intent(this, DashboardActivity::class.java)) }
        findViewById<Button>(R.id.btnSettings).setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        findViewById<Button>(R.id.btnDonate).setOnClickListener { DonateDialog(this).show() }
        
        findViewById<TextView>(R.id.txtVer).text = "v${packageManager.getPackageInfo(packageName, 0).versionName}"
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 77)
        refresh()
        // Silently check for GitHub updates!
        GitHubUpdateChecker.check(this)
    }

    private fun refresh() {
        val on = TorchManager.isOn
        power.isActivated = on
        badge.text = when {
            TorchManager.mode == "STROBE" -> "MODE: STROBE"
            TorchManager.mode == "SOS" -> "MODE: SOS"
            TorchManager.mode == "MORSE" -> "MODE: MORSE"
            on -> "TORCH ACTIVE"
            else -> "TORCH INACTIVE"
        }
        badge.setTextColor(if (on) 0xFFFF9933.toInt() else 0xFF888888.toInt())
    }

    // Helper to recursively apply Poppins font to all TextViews
    private fun applyFonts(v: View) {
        if (v is TextView) v.typeface = Fonts.medium(this)
        else if (v is ViewGroup) for (i in 0 until v.childCount) applyFonts(v.getChildAt(i))
    }
}